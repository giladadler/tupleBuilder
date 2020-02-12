function AddItem() {
		
	var counter = localStorage.getItem("counter");
	if (counter === null) {
		counter = 0;
	}
	counter++;
	localStorage.setItem("counter", counter);
	
	//if ($('#defaultStatics').prop("checked") == true ){
	//	alert('1111111111');
	//}
	
	var hostame = document.forms.ShoppingList.hostname.value;
	var agentVersion = document.forms.ShoppingList.agentVersion.value;
	var agentType = document.forms.ShoppingList.agentType.value;
	var account = document.forms.ShoppingList.account.value;
	var ep = "hostname=" + hostame + "&version=" + agentVersion + "&type=" + agentType + "&accountName=" + account
	localStorage.setItem("ep-" + counter, ep);
	showEP();
	showStatics() ;
}
function AddStatic() {
		
	var staticEPNumber = document.forms.ShoppingList.staticEPNumber.value;
	var previousvalue = localStorage.getItem("static-ep-" + staticEPNumber)
	if (previousvalue == null ){
		previousvalue = "";
	}
	var static = document.forms.ShoppingList.StaticValues.value;
	var staticValue = document.forms.ShoppingList.staticValue.value;
	var value = previousvalue  + static + "=" + staticValue + "&"
	localStorage.setItem("static-ep-" + staticEPNumber, value);
	showStatics();
	
}


function ClearAll() {
	localStorage.clear();
	showEP();
	 showStatics();
}

// dynamically draw the table
function showALL() {
	showEP();
	 showStatics();
}

function showStatics() {
	if (CheckBrowser()) {
		
		var ep = "";
		var staticEpOptions = "";
		for (i = 0; i <= localStorage.length - 1; i++) {
			ep = localStorage.key(i);
			if (ep.includes("ep-") && !ep.includes("static-")){
				staticEpOptions += "<option value='" + ep +"'>" + ep +"</option>";
			}
			
		}
		document.getElementById('staticEPNumber').innerHTML = staticEpOptions;
		var key = "";
		var statics = "<tr><th>Endpoint number</th><th>Statics</th></tr>\n";	
		for (i = 0; i <= localStorage.length - 1; i++) {
			key = localStorage.key(i);
			if (key.includes("static-")){
				statics += "<tr><td id='" + i + "'>" + key.substring(7) + "</td>\n<td>"
					+ localStorage.getItem(key) + "</td></tr>\n";
			}
			
		}
		if (statics == "<tr><th>Name</th><th>Value</th></tr>\n") {
			statics += "<tr><td><i>empty</i></td>\n<td><i>empty</i></td></tr>\n";
		}
		document.getElementById('statics').innerHTML = statics;
	} else {
		alert('Cannot store shopping list as your browser do not support local storage');
	}
}

function showEP() {
	if (CheckBrowser()) {
		var key = "";
		var list = "<tr><th>Endpoint number</th><th>Value</th></tr>\n";
		for (i = 0; i <= localStorage.length - 1; i++) {
			key = localStorage.key(i);
			if (key.includes("ep-") && !key.includes("static-")){
				list += "<tr><td id='" + i + "'>" + key + "</td>\n<td>"
					+ localStorage.getItem(key) + "</td></tr>\n";
			}
			
		}
		if (list == "<tr><th>Name</th><th>Value</th></tr>\n") {
			list += "<tr><td><i>empty</i></td>\n<td><i>empty</i></td></tr><hr>\n";
		}
		document.getElementById('list').innerHTML = list;
	} else {
		alert('Cannot store shopping list as your browser do not support local storage');
	}
}

/*
 * Checking the browser compatibility.
 * 
 * Alternately can use Modernizr scripts- JavaScript library that helps us to
 * detect the browser support for HTML5 and CSS features Example - <script
 * type="text/javascript" src="modernizr.min.js"></script>
 * 
 * if (Modernizr.localstorage) { //use localStorage object to store data } else {
 * alert('Cannot store user preferences as your browser do not support local
 * storage'); }
 */
function CheckBrowser() {
	if ('localStorage' in window && window['localStorage'] !== null) {
		// we can use localStorage object to store data
		return true;
	} else {
			return false;
	}
}