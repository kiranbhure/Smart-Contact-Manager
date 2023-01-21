
   function openNav() {
    document.getElementById("right").style.marginLeft = "15%";
    document.getElementById("left").style.display = "block";
  }
  
  function closeNav() {
    document.getElementById("left").style.display = "none";;
    document.getElementById("right").style.marginLeft = "0%";
  }

  const search = () => {
    let query = $("#search-input").val();

    if(query == ''){
      $(".search-result").hide();
    }
    else{
      // sending request to server
      let url = `http://localhost:8080/search/${query}`;
      fetch(url)
      .then( (response) => {
        return response.json();
      })
      .then( (data) =>{
        let text = `<div class='list-group'>`;

        data.forEach(contact => {
          text += `<a href='/user/contact/${contact.cid}'  class='list-group-item list-group-item-action'> ${contact.name} </a>`
        });

        text += `</div>`;
        $(".search-result").html(text);
        $(".search-result").show(); 
      });

    }
    
  };
  

 

