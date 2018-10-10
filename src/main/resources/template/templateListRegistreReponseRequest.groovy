<soapenv:Envelope
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:xsd="http://www.w3.org/2001/XMLSchema"
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:web="https://www.marches-securises.fr/webserv/">
    <soapenv:Header/>
    <soapenv:Body>
        <web:lister_reponses_electroniques soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
            <login xsi:type="xsd:string">$login</login>
            <password xsi:type="xsd:string">$password</password>
            <dce xsi:type="xsd:string">$msReference</dce>
            <idpa xsi:type="xsd:string"></idpa>
            <ref xsi:type="xsd:string"></ref>
            <params xsi:type="Map"
                xmlns="http://xml.apache.org/xml-soap">
                <item>
                    <key xsi:type="xsd:string">ordre</key>
                    <value xsi:type="xsd:string">$ordre</value>
                </item>
                <item>
                    <key xsi:type="xsd:string">sensordre</key>
                    <value xsi:type="xsd:string">$sensOrdre</value>
                </item>
            </params>
        </web:lister_reponses_electroniques>
    </soapenv:Body>
</soapenv:Envelope>