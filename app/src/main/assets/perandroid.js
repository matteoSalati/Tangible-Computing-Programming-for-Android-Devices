
function getUrlParameter(sParam)
{
    var sPageURL = decodeURIComponent(window.location.search.substring(1)),
        sURLVariables = sPageURL.split('&'),
        sParameterName,
        i;

    for (i = 0; i < sURLVariables.length; i++)
	{
        sParameterName = sURLVariables[i].split('=');

        if (sParameterName[0] === sParam)
		{
            return sParameterName[1] === undefined ? true : sParameterName[1];
        }
    }
	
	return null;
};



$( window ).on( "load", function()
{

	
	setTimeout(function()
	{
        //$("#dialog").hide();

		//localStorage.clear();
		
		var paramJson = getUrlParameter("code");
		if(paramJson != null)
		{
			var instructions = JSON.parse(paramJson);
			console.log( instructions);
			
			code = parseCode(instructions);
			if (instructions.length > 0) code = 'error';
			console.log(code);
			code = '<xml xmlns="http://www.w3.org/1999/xhtml">' + code + '</xml>';
			Blockly.getMainWorkspace().clear();
			var xml = Blockly.Xml.textToDom(code);
			Blockly.Xml.domToWorkspace(xml, Blockly.getMainWorkspace());

			runButton.click();
		}
		else
		{
			console.log("non c'è il parametro code");
		}
    }, 500)
    
	
	 
});


function prova()
{
	console.log( "ready222!" );
	localStorage.clear();
	
	var paramJson = getUrlParameter("code");
	if(paramJson != null)
	{
		var instructions = JSON.parse(paramJson);
		console.log( instructions);
		
		var code = parseCode(instructions);
        if (instructions.length > 0) code = 'error';
        console.log(code);
        code = '<xml xmlns="http://www.w3.org/1999/xhtml">' + code + '</xml>';
        //Blockly.getMainWorkspace().clear();
        var xml = Blockly.Xml.textToDom(code);
        Blockly.Xml.domToWorkspace(xml, Blockly.getMainWorkspace());
	}
	else
	{
		console.log("non c'è il parametro code");
	}
}


