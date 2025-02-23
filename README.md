**Okta® is the registered trademark of Okta, Inc. This project is not affiliated with Okta, Inc. and Okta, Inc. does not endorse this project.**

# Example Okta On-Premises Provisioning SCIM 2.0 Server with Entitlement Support



This is an example application supports the early release [On-premises provisioning and entitlements](https://help.okta.com/oie/en-us/content/topics/provisioning/opp/opp-entitlements.htm) feature.

## Software
1. Java JDK 17+
2. Apache Maven
3. Java Editor, like Eclipse
4. Text Editor, like Visual Studio Code

## Development

### Build

`mvn clean install`

### Local Testing

Start the server from maven:

`mvn clean spring-boot:run -Dspring.profiles.active=dev`

Start the server with Java remote debugging enabled:

`mvn clean spring-boot:run  -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments=-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005`

### Configuration

This application retrieves H2 connection details from the Spring Boot application-<env>.properties file located in the [src/main/resources](src/main/resources) directory. 

Here is an example database connection entry:

```
h2-connector.instances.db1.name=acmepreview_h2db1_2
h2-connector.instances.db1.jdbc_url=jdbc:h2:file:h2data/db1
h2-connector.instances.db1.jdbc_username=sa
h2-connector.instances.db1.jdbc_password=password

```

The instance ID, in this case db1, is added to the basic authentication user store with a default password of "okta". This application then uses the authenticated user to identify which database connection to use to service the SCIM request.

The name value should be the Okta application's variable/name which would be used in the user scheme extension URL for additional custom attributes.


### TLS Certificate
Run this command to generate a new Spring Boot TLS self signed certificate 
`keytool -genkeypair -alias okta-scim -keyalg RSA -keystore "src/main/resources/keystore.p12"  -storetype PKCS12 -keypass changeit -storepass changeit -validity 10000 -dname "CN=Okta  SCIM,OU=aaronanderson,O=Github,C=US" -ext "SAN:c=DNS:localhost,IP:127.0.0.1"`


## Okta Application setup

1. Log into the Okta Admin application
1. Go to Settings, Features, and ensure "OPP Agent with SCIM 2.0 support" is enabled.
1. Go to Applications, click the "Create App Integration" button
1. Select SAML 2.0
1. Enter the app name and check "Do not display application icon to users"
1. Enter "https://localhost" as the value of "Single sign-on URL" and "Audience URI (SP Entity ID)"
1. Select the General tab and in "App Settings" click edit, select the Provisioning option "On-Premises Provisioning", and click save
1. Select the Provisioning tab, Integration subtab, and enter the following values:
SCIM connector base URL: https://localhost:8444/scim/v2/
Authorization type: Basic Auth
Basic Auth credentials: <username from application.properties> 
Unique user field name: userName
Accept user updates: checked
Timeout for API calls: 90 seconds
Connect to these agents: select all
1. Click the "Test Connector Configuration" button
1. Review the enabled features and click Close
1. Click the Save button. The first attempt to save the configuration may result in an Internal Error. If it does, wait 10-15 seconds and save it again.
1. Click the import tab and perform a full import.
1. Select the Directory, Profile Editor menu option
1. Find the new application, and select
1. Click the "Add Attribute" button. Ensure the schema attributes are loaded. Click "Refresh Attribute List" or cancel and retry as needed.
1. Record the application variable name for future reference.
1. Go back to the application and select the Provisioning tab and then the "To App" subtab. 
1. Click Edit and enable all Provisoning to App features.
1. Select Governance and click the "Sync entitlements" link



## Windows Development OPP Agent
Currently the Okta OPP Agent only supports Windows Server Edit and Linux. For standard Windows development purposes, follow these instructions to create a local Linux VM to run the agent and redirect OPP SCIM requests to the application running on the host OS.

1. Download the latest [CentOS version](https://www.centos.org/download/) 
1. Enabled Windows Hyper-V
1. Create a new Hyper-V Generation 2 virtual machine with 4096 MB of RAM, Connection set to Default Switch, 20GB HD, and install OS from bootable DVD -> CentOS ISO file.
1. Before starting the VM got to Settings..., Security, disable secure boot and select the "Open Source Shielded VM" template. Also enable the "Guest Services" in the Integration Services settings.
1. Start the VM and peform the CentOS installation. Create a new root user, i.e. rdev

### OPP Agent Install - CENTOS Stream 10

```
sudo yum install hyperv-daemons
sudo yum install libXt
sudo yum install epel-release
sudo yum install gtk2-devel
sudo yum install initscripts
sudo rpm -i OktaProvisoningAgent*.rpm
sudo /opt/OktaProvisoningAgent/configure_agent.sh
```

Create a systemd service to auto-restart the agent as [documented here](https://support.okta.com/help/s/article/unable-to-start-okta-provisioning-agent-service?language=en_US)

```
sudo vi /lib/systemd/system/OktaProvisioningAgent.service
[Unit]
Description=Okta Provisioning Agent

[Service]
Type=forking
ExecStart=/opt/OktaProvisioningAgent/OktaProvisioningAgent

[Install]
WantedBy=multi-user.target 

sudo systemctl daemon-reload
sudo systemctl enable OktaProvisioningAgent
sudo systemctl start OktaProvisioningAgent
sudo systemctl status OktaProvisioningAgent
```

setup [localhost forwarding](https://discussion.fedoraproject.org/t/firewalld-forward-local-traffic-to-remote-host/87974/6) to the host SCIM server. 

On host, run
`ipconfig`
Note the main ethernet or wireless address, i.e 192.168.4.21. Use it in the firewalld rules below.


```
sudo tee /etc/sysctl.d/00-custom.conf << EOF > /dev/null
net.ipv4.conf.all.route_localnet = 1
EOF

sudo systemctl restart systemd-sysctl.service

sudo firewall-cmd --permanent --direct --add-rule ipv4 nat OUTPUT 0 -m addrtype --src-type LOCAL --dst-type LOCAL -p tcp --dport 8444 -j DNAT --to-destination 192.168.4.21:8444
sudo firewall-cmd --permanent --direct --add-rule ipv4 nat POSTROUTING 0 -m addrtype --src-type LOCAL --dst-type UNICAST -j MASQUERADE
sudo firewall-cmd --reload

#sudo setenforce 0
```

Add the SCIM server's self signed certificate to the OPP agent's trust store

```
openssl s_client -showcerts -connect localhost:8444 </dev/null 2>/dev/null|openssl x509 -outform PEM >opp_scim.pem
sudo /opt/OktaProvisioningAgent/jre/bin/keytool -import -file opp_scim.pem -alias opp_scim -keystore /opt/OktaProvisioningAgent/jre/jre/lib/security/cacerts
#password is changeit, enter y to trust the certificate

sudo systemctl restart OktaProvisioningAgent

```
### Reinstall the Agent

If ever needed, run this command to uninstall the agent so it can be reinstalled

`sudo yum remove OktaProvisoningAgent.x86_64`


### Hyper-V Networking
On guest, run
`ip addr`

Note the eth0 IP address, i.e. 172.19.203.37


SSH from the host to the guest
`ssh rdev@172.19.203.37`

# Known Issues

1. SCIM 2.0 push groups are not working. When activating the push group Okta generates an error for a missing SCIM 1.1 namespace.

# References

1. [Build a SCIM 2.0 server with entitlements](https://developer.okta.com/docs/guides/scim-with-entitlements/main/)
1. [OIG RCAR](https://support.okta.com/help/s/article/resource-centric-access-requests-rcar-6-17-2024-blog-post?language=en_US)
1. [OIG Resource collections](https://help.okta.com/en-us/content/topics/identity-governance/rc/resource-collection.htm)
1. [OPP SCIM 2.0 Requirement](https://support.okta.com/help/s/article/opp-agent-with-scim-2-0-support-feature-enabled-however-when-communicating-with-the-on-premises-application-connector-utilizing-scim-1-1)
1. [OIG API](https://developer.okta.com/docs/api/iga/)
1. [Reference SCIMple Spring Boot Application](https://github.com/apache/directory-scimple/tree/develop/scim-server-examples/scim-server-spring-boot)
1. [Spring Boot Dynamic Datasource Routing](https://attyuttam.medium.com/dynamic-data-source-routing-using-abstractroutingdatasource-in-spring-boot-d6dbdd644072)
