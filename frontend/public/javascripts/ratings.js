var __slice = [].slice;

(function ($, window) {
    var Starrs;

    Starrs = (function () {
        Starrs.prototype.defaults = {
            rating: void 0,
            numStars: 5,
            change: function (e, value) {
            }
        };

        function Starrs($el, options) {
            var i, _, _ref,
                _this = this;

            this.options = $.extend({}, this.defaults, options);
            this.$el = $el;
            _ref = this.defaults;
            for (i in _ref) {
                _ = _ref[i];
                if (this.$el.data(i) != null) {
                    this.options[i] = this.$el.data(i);
                }
            }
            this.createStars();
            this.syncRating();
            this.$el.on('mouseover.starrs', 'i', function (e) {
                return _this.syncRating(_this.$el.find('i').index(e.currentTarget) + 1);
            });
            this.$el.on('mouseout.starrs', function () {
                return _this.syncRating();
            });
            this.$el.on('click.starrs', 'i', function (e) {
                return _this.setRating(_this.$el.find('i').index(e.currentTarget) + 1);
            });
            this.$el.on('starrs:change', this.options.change);
        }

        Starrs.prototype.createStars = function () {
            var _i, _ref, _results;

            _results = [];
            for (_i = 1, _ref = this.options.numStars; 1 <= _ref ? _i <= _ref : _i >= _ref; 1 <= _ref ? _i++ : _i--) {
                _results.push(this.$el.append("<i class=\"material-icons\">star_outline</i>"));
            }
            return _results;
        };

        Starrs.prototype.setRating = function (rating) {
            if (!this.options.rating) {
                this.options.rating = rating;
                this.syncRating();

                var starrs = $(".starrs")[0];
                var url = document.getElementById("hiddenURL").innerText + rating;
                console.log("hiddenUrl:" + url);
                $.getJSON(url, {}, function (data) {
                    var rating_num = $(".starrs").parent().children(".rating_num");
                    var number = $(rating_num[0]).text();
                    var rating_sum = $(".starrs").parent().children(".rating_sum").children(".rating_sum");
                    var sum = $(rating_sum[0]).text();
                    $(rating_num[0]).text(((Number.parseFloat(number) * Number.parseInt(sum) + Number.parseInt(rating)) / (Number.parseInt(sum) + 1)).toFixed(1));
                    $(rating_sum[0]).text(Number.parseInt(sum) + 1);
                });
                return this.$el.trigger('starrs:change', rating);
            }
        };

        Starrs.prototype.syncRating = function (rating) {
            var i, _i, _j, _ref;

            rating || (rating = this.options.rating);
            if (rating) {
                for (i = _i = 0, _ref = rating - 1; 0 <= _ref ? _i <= _ref : _i >= _ref; i = 0 <= _ref ? ++_i : --_i) {
                    this.$el.find('i').eq(i).text("star");
                }
            }
            if (rating && rating < 5) {
                for (i = _j = rating; rating <= 4 ? _j <= 4 : _j >= 4; i = rating <= 4 ? ++_j : --_j) {
                    this.$el.find('i').eq(i).text("star_outline");
                }
            }
            if (!rating) {
                return this.$el.find('i').text("star_outline");
            }
        };

        return Starrs;

    })();
    return $.fn.extend({
        starrs: function () {
            var args, option;

            option = arguments[0], args = 2 <= arguments.length ? __slice.call(arguments, 1) : [];
            return this.each(function () {
                var data;

                data = $(this).data('star-rating');
                if (!data) {
                    $(this).data('star-rating', (data = new Starrs($(this), option)));
                }
                if (typeof option === 'string') {
                    return data[option].apply(data, args);
                }
            });
        }
    });
})(window.jQuery, window);

$(function () {
    return $(".starrs").starrs();
});

$(document).ready(function () {

    $('#hearts').on('starrs:change', function (e, value) {
        $('#count').html(value);
    });

    $('#hearts-existing').on('starrs:change', function (e, value) {
        $('#count-existing').html(value);
    });
});