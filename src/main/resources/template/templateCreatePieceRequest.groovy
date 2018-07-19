<soapenv:Envelope xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://www.marches-securises.fr/webserv/" xmlns:s2="http://xml.apache.org/xml-soap">
<soapenv:Header/>
<soapenv:Body>
<web:nouveau_fichier_log soapenv:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
<login xsi:type="xsd:string">$login</login>
         <mdp xsi:type="xsd:string">$password</mdp>
<pa xsi:type="xsd:string">$pa</pa>
         <dce xsi:type="xsd:string">$dce</dce>
<fichier xsi:type="s2:Map">
<item>
<key xsi:type="xsd:string">lot</key>
            <value xsi:type="xsd:string">$cleLot</value>
</item>
          <item>
            <key xsi:type="xsd:string">libelle</key>
<value xsi:type="xsd:string">$libelle</value>
          </item>
<item>
<key xsi:type="xsd:string">la</key>
            <value xsi:type="xsd:string">$la</value>
</item>
          <item>
            <key xsi:type="xsd:string">ordre</key>
<value xsi:type="xsd:int">$ordre</value>
          </item>
<item>
<key xsi:type="xsd:string">nom</key>
            <value xsi:type="xsd:string">$nom</value>
</item>
          <item>
            <key xsi:type="xsd:string">extension</key>
<value xsi:type="xsd:string">$extension</value>
          </item>
<item>
<key xsi:type="xsd:string">contenu</key>
            <value xsi:type="xsd:string">$contenu</value>
</item>
          <item>
            <key xsi:type="xsd:string">poids</key>
<value xsi:type="xsd:int">$poids</value>
          </item>
</fichier>
      </web:nouveau_fichier_log>
</soapenv:Body>
</soapenv:Envelope>