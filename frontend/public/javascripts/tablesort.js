/**
 * Merge Table by table value
 */
function makeSortable(table, start, end) {
    var headers = table.getElementsByTagName("th");
    for (var i = start; i < end; i++) {
        (function (n) {
            var flag = false;
            headers[n].onclick = function () {
                var before_selected = $('.headersort');
                before_selected.removeClass("headersort");
                $(this).addClass("headersort");
                var tbody = table.tBodies[0];
                var rows = tbody.getElementsByTagName("tr");
                rows = Array.prototype.slice.call(rows, 0);//Store Real Array
                //Sort by n th element
                var header = headers[n];
                if (header.innerHTML.includes("date") || header.innerHTML.includes("Date")
                    || header.innerHTML.includes("Time") || header.innerHTML.includes("time")) {
                    rows.sort(function (row1, row2) {
                        var cell1 = row1.getElementsByTagName("td")[n];
                        var cell2 = row2.getElementsByTagName("td")[n];
                        var val1 = cell1.textContent || cell1.innerText;
                        var val2 = cell2.textContent || cell2.innerText;
                        var d1 = new Date(val1);
                        var d2 = new Date(val2);
                        return d1 - d2;
                    });
                }
                else {
                    rows.sort(function (row1, row2) {
                        var cell1 = row1.getElementsByTagName("td")[n];
                        var cell2 = row2.getElementsByTagName("td")[n];
                        var val1 = cell1.textContent || cell1.innerText;
                        var val2 = cell2.textContent || cell2.innerText;
                        if (val1.localeCompare(val2) > 0) {
                            return 1;
                        } else if (val1.localeCompare(val2) < 0) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                }

                if (flag) {
                    rows.reverse();
                }
                for (var i = 0; i < rows.length; i++) {
                    tbody.appendChild(rows[i]);
                }
                flag = !flag;

            }
        }(i));
    }
} 