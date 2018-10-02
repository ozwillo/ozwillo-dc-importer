<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
        <soapenv:Header/>
        <soapenv:Body>
            <web:lire_consultation_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
                <login xsi:type="xsd:string">$login</login>
                <password xsi:type="xsd:string">$password</password>
                <pa xsi:type="xsd:string">$pa</pa>
                <dce xsi:type="xsd:string">$dce</dce>
            </web:lire_consultation_log>
    </soapenv:Body>
</soapenv:Envelope>