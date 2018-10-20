<soapenv:Envelope
xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
xmlns:web="https://www.marches-securises.fr/webserv/"
xmlns:xsd="http://www.w3.org/2001/XMLSchema"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <soapenv:Header/>
        <soapenv:Body>
            <web:lister_retraits_electroniques_details soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                <login xsi:type="xsd:string">$login</login>
                <password xsi:type="xsd:string">$password</password>
                <dce xsi:type="xsd:string">$msReference</dce>
                <params
                    xmlns="http://xml.apache.org/xml-soap" xsi:type="Map">
                    <item>
                        <key xsi:type="xsd:string">ordre</key>
                        <value xsi:type="xsd:string">$ordre</value>
                    </item>
                    <item>
                        <key xsi:type="xsd:string">sensordre</key>
                        <value xsi:type="xsd:string">$sensOrdre</value>
                    </item>
                </params>
            </web:lister_retraits_electroniques_details>
        </soapenv:Body>
</soapenv:Envelope>