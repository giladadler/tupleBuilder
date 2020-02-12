var displayAlert = (function(){       
  var alertTemplate = $('#alertTmpl').html(),
      alertDiv = $('#alertDiv');
  return function(msg){
      alertDiv.html(Mustache.render(alertTemplate, {msg: msg}));
      alertDiv.find('div').addClass('alert alert-success')
  }
})();