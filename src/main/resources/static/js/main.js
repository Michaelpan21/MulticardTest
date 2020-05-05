
var fileKeeper = {};

var app = new Vue({
  el: '#app',
  template: '<div><input @change="prepare($event.target.files[0])" type="file" accept=".xlsx"/>' +
              '<button type="submit" @click="send">Загрузить</button></div>',
  methods: {
    prepare: function(file) {
      var reader = new FileReader();

      reader.onload = function (e) {
        var data = e.target.result;
        data = new Uint8Array(data);
        var workbook = XLSX.read(data, {type: 'array'});

        workbook.SheetNames.forEach(function (sheetName) {
          var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {header: 1});
          if (roa.length) fileKeeper[sheetName] = roa;
        });
      };
      reader.readAsArrayBuffer(file);
    },
    send: function() {
      this.$http.post('/atm', fileKeeper);
    }
  },
});