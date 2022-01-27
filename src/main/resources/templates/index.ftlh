<head>
<title>Countdown solver</title>
<meta name="viewport" content="width=100, initial-scale=1">
</head>
<body>
<form action="/" method="put">
<#list 0..6 as n>
<input name="num${n}" type="number" style="width: 12%" value="${(input![])[n]!""}"/>
</#list>
<p><input type="submit" value="OK"/>
</form>
<b>
<#list messages as msg>
<p>${msg}</p>
</#list>
</body>