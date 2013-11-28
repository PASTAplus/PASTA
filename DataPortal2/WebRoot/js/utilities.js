/* JavaScript utility routines */

    // Return the number of selected options in a multi-list box
    function howManySelected(selectObject) {
      var numberSelected = 0;
      for (var i = 0; i < selectObject.options.length; i++) {
      if (selectObject.options[i].selected)
       numberSelected++;
      }
      return numberSelected;
    }

    function submitform(form_ref) {
        form_ref.submit();
    }

    function trim(stringToTrim) {
        return stringToTrim.replace(/^\s*/, '').replace(/\s*$/,'');
    }

    