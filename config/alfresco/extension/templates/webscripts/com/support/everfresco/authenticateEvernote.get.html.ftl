<!-- Information used by consumer -->
<h3>Evernote EDAM API Web Test State</h3>
	Consumer key: ${consumerKey} <br/>
	Request token URL: ${requestTokenUrl} <br/>
	Access token URL: ${accessTokenUrl} <br/>
	Authorization URL Base: ${authorizationUrlBase} <br/>
	<br/>
	<br/>
	
	User request token: ${requestToken!"No session var"}<br/>
	User request token secret:  ${requestTokenSecret!"No session var"}<br/>
	User oauth verifier:  ${verifier!"No session var"}<br/>
	User access token:  ${accessToken!"No session var"}<br/>
	User NoteStore URL:  ${noteStoreUrl!"No session var"}

<!-- Manual operation controls -->
<hr/>

<h3>Actions</h3>

<ol>
	<!-- Step 3 in OAuth authorization: exchange the authorized request token for an access token -->
	<li>
	<#if (verifier?? || accessToken??) >
    	Get OAuth Access Token from Provider
	<#else>
	    <a href="?action=getAccessToken">
	      Get OAuth Access Token from Provider
	    </a>
	</#if>
	</li>
	
	<!-- Step 4 in OAuth authorization: use the access token that you obtained -->
	<!-- In this sample, we simply list the notebooks in the user's Evernote account -->
	<li>
	<#if (accessToken??) >
  		<a href="?action=listNotebooks">List notebooks in account</a><br/>
	<#else>
		List notebooks in account
	</#if>
	</li>
</ol>

<a href="?action=reset">Reset user session</a>

<!-- Results -->
<hr/>
<h3>Results</h3>

	<#if (notebooks??) >
	<ul>
		<#list notebooks as notebook>
	  		<li>${notebook.name} </li>
		</#list>
	</ul>
	</#if> 	