
var btnBar = new Vue({
  el: '#btnBar',
  template: '<div class="btn-div" v-show="show">' +
              '<button @click="">Отобразить данные</button>' +
              '<button @click="">Удалить данные</button>' +
            '</div>',
  data: {
    show: true,
  },
});

var app = new Vue({
  el: '#app',
  template: '<div>' +
              '<input id="fileInput" type="file" accept=".xlsx"/>' +
              '<button @click="send">Загрузить</button>' +
              '<div><label>Загруженно {{count}}</label></div>' +
            '</div>',
  data: {
    count: '',
  },
  methods: {
    send: function() {
        let formData = new FormData();
        formData.append('file', document.getElementById('fileInput').files[0])
        this.$http.post('/atm', formData).then(response => {
          this.count = response.body;
          if (this.count > 0) btnBar.show = true;
          else btnBar.show = false;
        }, response => {
               btnBar.show = false;
        });
    },
    /*prepare: function(file) {
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
    },*/

  },
});