<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/">
   <soapenv:Header/>
   <soapenv:Body>
      <web:modifier_lot_consultation_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
         <login xsi:type="xsd:string">$login</login>
         <password xsi:type="xsd:string">$password</password>
         <pa xsi:type="xsd:string">$pa</pa>
         <dce xsi:type="xsd:string">$dce</dce>
         <clelot xsi:type="xsd:string">$cleLot</clelot>
         <libelle xsi:type="xsd:string">$libelle</libelle>
         <ordre xsi:type="xsd:string">$ordre</ordre>
         <numero xsi:type="xsd:string">$numero</numero>
      </web:modifier_lot_consultation_log>
   </soapenv:Body>
</soapenv:Envelope>