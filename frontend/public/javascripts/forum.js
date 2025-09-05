$(function(){
    $(".hide-hood").hide();
    $(".toggle").click(function(){
        var hood = $(this).parent().children(".hide-hood").toggle();
        hood.children("input").focus();
    })

    $(".vote-thumb").click(function(){
        var id = $(this).attr("data-commentId");
        var act = $(this).attr("data-act");
        var self = $(this)

        if($(this).context.classList.contains("haveVoted")){
            return;
        }

        $(this).context.classList.add("haveVoted");

        if (act === "voteup") {
            //var url = "/workflow/thumbUp/"+id+"/"+$(self).attr("data-wfid");
            var url = "/comment/addThumbUp/"+id;
            console.log("urlthumbup:" + url);
            $.getJSON(url, {}, function(data){
                var vote_num = self.parent().children(".vote-num");
                var number = $(vote_num[0]).text();
                $(vote_num[0]).text(Number.parseInt(number)+1);
                self.parent().children(".vote-thumb").removeClass("voted");
                self.addClass("voted")
            });
        } else {
            //var url = "/comment/addThumbDown/"+id+"/"+$(self).attr("data-wfid");
            var url = "/comment/addThumbDown/"+id;
            $.getJSON(url, {}, function(data){
                var vote_num = self.parent().children(".vote-num");
                var number = $(vote_num[0]).text();
                $(vote_num[0]).text(Number.parseInt(number)-1);
                self.parent().children(".vote-thumb").removeClass("voted");
                self.addClass("voted")
            });
        }
    })

    $(".vote-thumb-comment").click(function(){
        var id = $(this).attr("data-commentId");
        var act = $(this).attr("data-act");
        var self = $(this)

        if($(this).context.classList.contains("haveVoted")){
            return;
        }

        $(this).context.classList.add("haveVoted");

        if (act === "voteup") {
            //var url = "/workflow/thumbUp/"+id+"/"+$(self).attr("data-wfid");
            var url = "/comment/addThumbUp/"+id;
            $.getJSON(url, {}, function(data){
                var vote_num_up = self.parent().children(".vote-num-up");
                var number = $(vote_num_up[0]).text();
                $(vote_num_up[0]).text(Number.parseInt(number)+1);
                self.parent().children(".vote-thumb-comment").removeClass("voted");
                self.addClass("voted")
            });
        } else {
            var url = "/comment/addThumbDown/"+id;
            $.getJSON(url, {}, function(data){
                var vote_num_down = self.parent().children(".vote-num-down");
                var number = $(vote_num_down[0]).text();
                $(vote_num_down[0]).text(Number.parseInt(number)+1);
                self.parent().children(".vote-thumb-comment").removeClass("voted");
                self.addClass("voted")
            });
        }
    })

    $(".vote-thumb-post").click(function(){
        var id = $(this).attr("data-postId");
        var act = $(this).attr("data-act");
        var self = $(this)

        if($(this).context.classList.contains("haveVoted")){
            return;
        }

        $(this).context.classList.add("haveVoted");

        if (act === "voteup") {
            var url = "/forum/thumbUp/"+id;
            $.getJSON(url, {}, function(data){
                var vote_num_thumb_up = self.parent().children(".vote-num-thumb-up");
                var number = $(vote_num_thumb_up[0]).text();
                $(vote_num_thumb_up[0]).text(Number.parseInt(number)+1);


                self.parent().children(".vote-thumb-post").removeClass("voted");
                self.addClass("voted")
            });
        } else {
            var url = "/forum/thumbDown/"+id;
            $.getJSON(url, {}, function(data){
                var vote_num_thumb_down = self.parent().children(".vote-num-thumb-down");
                var number = $(vote_num_thumb_down[0]).text();
                $(vote_num_thumb_down[0]).text(Number.parseInt(number)+1);

                self.parent().children(".vote-thumb-post").removeClass("voted");
                self.addClass("voted")
            });
        }
    })

    $(".suggestion-like a").click(function(){
        var url = "/suggestion/voteToSuggestion/" + $(this).attr("data-sugId");
        var self = $(this);
        $.getJSON(url, {}, function(data){
            var numSpan = ($(self).parent().children("span"))[0];
            var number = Number.parseInt($(numSpan).text());
            $(self).addClass("like-voted");
            $(numSpan).text(number+1);
        })
    })
});