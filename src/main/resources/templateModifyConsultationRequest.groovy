<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/">
<soapenv:Header/>
<soapenv:Body>
<web:modifier_consultation_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<login xsi:type="xsd:string">$login</login>
         <password xsi:type="xsd:string">$password</password>
<pa xsi:type="xsd:string">$pa</pa>
         <dce xsi:type="xsd:string">$dce</dce>
<tableau xsi:type="Map" xmlns="http://xml.apache.org/xml-soap">
<item>
<key xsi:type="xsd:string">objet</key>
            <value xsi:type="xsd:string">$objet</value>
</item>
          <item>
            <key xsi:type="xsd:string">en_ligne</key>
<value xsi:type="xsd:int">$enligne</value>
          </item>
<item>
<key xsi:type="xsd:string">date_publication</key>
            <value xsi:type="xsd:timestamp">$datePublication</value>
</item>
          <item>
            <key xsi:type="xsd:string">date_cloture</key>
<value xsi:type="xsd:timestamp">$dateCloture</value>
          </item>
<item>
<key xsi:type="xsd:string">ref_interne</key>
            <value xsi:type="xsd:string">$reference</value>
</item>
          <item>
            <key xsi:type="xsd:string">finalite_marche</key>
<value xsi:type="xsd:string">$finaliteMarche</value>
          </item>
<item>
<key xsi:type="xsd:string">type_marche</key>
            <value xsi:type="xsd:string">$typeMarche</value>
</item>
          <item>
            <key xsi:type="xsd:string">type_prestation</key>
<value xsi:type="xsd:string">$prestation</value>
          </item>
<item>
<key xsi:type="xsd:string">passation</key>
            <value xsi:type="xsd:string">$passation</value>
</item>
          <item>
            <key xsi:type="xsd:string">a_lots</key>
<value xsi:type="xsd:int">$alloti</value>
          </item>
<item>
<key xsi:type="xsd:string">departements_prestation</key>
            <value xsi:type="xsd:string">$departement</value>
</item>
          <item>
            <key xsi:type="xsd:string">emails</key>
<value xsi:type="xsd:string">$email</value>
          </item>
</tableau>     
      </web:modifier_consultation_log>
</soapenv:Body>
</soapenv:Envelope>