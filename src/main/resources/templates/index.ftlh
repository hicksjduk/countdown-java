<head>
<title>Countdown solver</title>
<meta name="viewport" content="width=100, initial-scale=1">
</head>
<body>
<form action="/" method="post">
<#list 0..6 as n>
<input name="num${n}" type="number" size=3 value="${(input![])[n]!""}"/>
</#list>
<input type="submit" value="OK"/>
</form>
<b>
<#list messages as msg>
<p>${msg}</p>
</#list>
</body>