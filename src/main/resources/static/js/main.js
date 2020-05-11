
var btnBar = new Vue({
  el: '#btnBar',
  template: '<div class="btn-div">' +
              '<button v-bind:disabled="disable" @click="showData">Отобразить данные</button>' +
              '<button v-bind:disabled="disable" @click="deleteAll">Удалить данные</button>' +
            '</div>',
  data: {
    disable: true,
  },
  methods: {
    deleteAll: function() {
      this.$http.delete('/atm');
      app.fileName = '';
      this.disable = true;
      app.count = '';
      document.getElementById('dropAr').classList.remove('highlight');
    },
    showData: function() {
      window.location.href = '/view';
    },
  },
});

function checkFormat(fileName) {
  let parts = fileName.split('.')
  if (parts[parts.length - 1] === 'xlsx') return true;
  return false;
};

function handle(){
  let file = document.getElementById('fileInput').files[0]
  if (file.size === 0 && file.size > 2000000) app.fileName = 'Недопустимый размер';
  if (checkFormat(file.name)) {
    if (file) {
      app.file = file;
      app.fileName = file.name;
    }
  }
};


Vue.component('drop-box', {
  props: ['fileName'],
  template: '<div class="drop" id="dropAr">' +
              '<div id="excel-ic"><img src="/static/img/excel-icon.png" />' +
              '<div><label>{{fileName}}</label></div></div>' +
            '</div>',
});


var app = new Vue({
  el: '#app',
  template: '<div>' +
              '<div class="header"><label>Перетащите или выберите файл </label><label>.xlsx</label></div>' +
              '<drop-box :fileName="fileName"/>' +
              '<button @click="click" class="choose">Выбрать EXCEL файл</button>' +
              '<input onchange="handle()" id="fileInput" type="file" accept=".xlsx"/>' +
              '<button @click="send" class="save">Загрузить</button>' +
              '<div class="footer"><label v-show="count">Загруженно: {{count}} строк</label></div>' +
            '</div>',
  data: {
    count: '',
    file: null,
    fileName: '',
  },
  methods: {
    send: function() {
        let icon = document.getElementById('excel-ic');
        let drop = document.getElementById('dropAr');
        icon.classList.add('rotate');
        let formData = new FormData();
        formData.append('file', this.file)
        this.$http.post('/atm', formData).then(response => {
          this.count = response.body;
          if (this.count > 0) {
            btnBar.disable = false;
            drop.classList.add('highlight');
          }
          else btnBar.disable = true;
          icon.classList.remove('rotate');
        }, response => {
          btnBar.disable = true;
          drop.classList.add('highlight-er');
          icon.classList.remove('rotate');
          fileName = 'Неудалось сохранить';
        });
    },
    click: function() {
      fileElem = document.getElementById('fileInput')
      if (fileElem) {
        fileElem.click();
      }
    },
  },
  mounted: function() {
    let dropArea = document.getElementById('dropAr');

    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(eventName => {
      dropArea.addEventListener(eventName, preventDefaults, false)
    });

    ['dragenter', 'dragover'].forEach(eventName => {
      dropArea.addEventListener(eventName, highlight, false)
    });

    ['dragleave', 'drop'].forEach(eventName => {
      dropArea.addEventListener(eventName, unhighlight, false)
    });

    dropArea.addEventListener('drop', handleDrop, false);

    function handleDrop(e) {
      let dt = e.dataTransfer
      document.getElementById('fileInput').files = dt.files;
      handle();
    };

    function highlight(e) {
        dropArea.classList.add('highlight')
    };

    function unhighlight(e) {
      dropArea.classList.remove('highlight')
    };

    function preventDefaults (e) {
      e.preventDefault()
      e.stopPropagation()
    };
  },
});
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
