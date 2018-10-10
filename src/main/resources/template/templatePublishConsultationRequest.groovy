<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/">
   <soapenv:Header/>
   <soapenv:Body>
      <web:publication_dce_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
         <login xsi:type="xsd:string">$login</login>
         <password xsi:type="xsd:string">$password</password>
         <idpa xsi:type="xsd:string">$pa</idpa>
         <dce xsi:type="xsd:string">$dce</dce>
      </web:publication_dce_log>
   </soapenv:Body>
</soapenv:Envelope>