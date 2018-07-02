<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/">
    <soapenv:Header/>
    <soapenv:Body>
        <web:supprimer_all_lots_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
            <login xsi:type="xsd:string">$login</login>
            <password xsi:type="xsd:string">$password</password>
            <pa xsi:type="xsd:string">$pa</pa>
            <dce xsi:type="xsd:string">$dce</dce>
        </web:supprimer_all_lots_log>
    </soapenv:Body>
</soapenv:Envelope>