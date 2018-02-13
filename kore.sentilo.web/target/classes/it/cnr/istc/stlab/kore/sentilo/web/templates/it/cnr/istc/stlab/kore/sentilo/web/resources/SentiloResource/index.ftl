<#import "/imports/common.ftl" as common>
<#escape x as x?html>
<@common.page title="Welcome to SENTILO!" hasrestapi=false>
<div id="tipalo_main">
<h3>Sentilo</h3>
<p>SENTILO performs sentence-based sentiment analysis. It relies on (<a href="http://wit.istc.cnr.it/sentilo-release/fred">FRED</a>), a machine reader for the Semantic Web, and applies appropriate rules to mine FRED's graphs</p>
<table>
<tbody>
<tr>
<td width="43%" valign="top">
<form accept-charset="UTF-8" method="GET">
<fieldset>
<legend>Enter a text</legend>
<textarea cols="80" name="text" rows="15"></textarea>
</fieldset>
<fieldset>
<legend>Options</legend>
Namespace Prefix for FRED terms: <input type="text" size="20" name="prefix" value="fred:" /> <br/>
Namespace for FRED terms: : <input type="text" size="80" name="namespace" value="http://www.ontologydesignpatterns.org/ont/fred/domain.owl#" /> <br/>
Show only scores <input type="checkbox" name="scores" value="true" /><br/>
Use senticnet for scores (default strategy senticnet+sentiwordnet) <input type="checkbox" name="sentiwordnet" value="true" /><br/>
<select name="format">
<option value="image/png">PNG</option>
<option value="application/rdf+xml">RDF/XML</option>
<option value="application/rdf+json">RDF/JSON</option>
<option value="text/turtle">Turtle</option>
<option value="text/rdf+n3">N3</option>
<option value="text/rdf+nt">NT</option>
<option value="text/functional">Functional</option>
</select>
<input type="submit" value="Read it!" />
</fieldset>
</form>
</td>
<td width="43%" valign="top">
<fieldset>
<legend>Examples</legend>
<ul>
<li>
<a href="?text=It+was+a+very+bad+spring+here+in+Britain.+Fortunately+we+had+a+good+summer.">It was a very bad spring here in Britain. Fortunately we had a good summer.</a>
</li>
<li>
<a href="?text=Monaco's+striker+Radamel+Falcao+claims+to+be+happy+in+France+although+fresh+bad+rumours+are+linking+him+with+a+move+to+Real+Madrid.">Monaco's striker Radamel Falcao claims to be happy in France although fresh bad rumours are linking him with a move to Real Madrid.</a>
</li>
<li>
<a href="?text=I+could+watch+Leonardo+Di+Caprio's+films+all+day+as+he+is+a+very+talented+actor.">I could watch Leonardo Di Caprio's films all day as he is a very talented actor.</a>
</li>
<li>
<a href="?text=Apartheid+in+South+Africa+is+still+a+terrible+problem+today,+a+very+sad+Nelson+Mandela+said+yesterday.">Apartheid in South Africa is still a terrible problem today, a very sad Nelson Mandela said yesterday.</a>
</li>
<li>
<a href="?text=Owen+Paterson+is+a+dangerous+deniar+wishing+to+protect+the+status+quo+for+the+sake+of+his+party's+financiers.">Owen Paterson is a dangerous deniar wishing to protect the status quo for the sake of his party's financiers.</a>
</li>
</ul></fieldset></td>
</tr>
</tbody>
</table>
</div> 
</@common.page>
</#escape>
