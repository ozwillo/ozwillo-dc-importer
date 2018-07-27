<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/">
<soapenv:Header/>
<soapenv:Body>
<web:supprimer_fichier_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<login xsi:type="xsd:string">$login</login>
         <mdp xsi:type="xsd:string">$password</mdp>
<pa xsi:type="xsd:string">$pa</pa>
         <dce xsi:type="xsd:string">$dce</dce>
<piece xsi:type="xsd:string">$clePiece</piece>
      </web:supprimer_fichier_log>
</soapenv:Body>
</soapenv:Envelope>