
var icon = new Vue({
  el: '#ic',
  data: {
    isShown: false,
  },
  template: '<div v-show="isShown" class="ic rotate"><img src="/static/img/excel-icon.png"/></div>',
});

var grid = new Vue({
    el: '#app',
    data: {
      tableData: [{reason: 'Загрузка..'}],
      editedRows: new Set(),
      deletedRows: new Set(),
    },
    mounted: function() {
        getByMapping('/atm')
    },
    methods: {
      deleteRow: function(index, rows) {
          rows.splice(index, 1);
          this.deletedRows.add(rows[index].id);
      },
      editRow: function(index, rows) {
          editBox.row = Object.assign({}, rows[index]);
          editBox.isShown = true;
          editBox.index = index;
          editBox.rows = rows;
      },
      save: function(index, rows) {
          rows.splice(index, 1);
      }
    },
});

var editBox = new Vue({
  el: '#edit-box',
  data: {
    row: {
      id: '',
      atmId: '',
      reason: '',
      begin: '',
      end: '',
      atmSerialNumber: '',
      bankName: '',
      channel: '',
    },
    isShown: false,
    index: '',
    rows: [],
  },
  template: '<div v-show="isShown" class="edit-box">' +
              '<label>{{row.id}}</label>' +
              '<input v-model="row.atmId" >' +
              '<input v-model="row.reason" >' +
              '<input v-model="row.begin" >' +
              '<input v-model="row.end" >' +
              '<input v-model="row.atmSerialNumber" >' +
              '<input v-model="row.bankName" >' +
              '<input v-model="row.channel" >' +
              '<button @click="ok">Ок</button>' +
            '</div>',
  methods: {
    ok: function() {
       this.isShown = false;
       grid.editedRows.add(this.row);
       this.rows.splice(this.index, 1, this.row);
    }
  }
});

var btnBar = new Vue({
  el: '#btnBar',
  template: '<div class="btn-div">' +
              '<button @click="showAll">Показать все</button>' +
              '<button @click="showTop3Reasons">Показать ТОП 3 причины</button>' +
              '<button @click="save">Сохранить</button>' +
              '<button @click="showTop3Time">Показать ТОП 3 времени ремонта</button>' +
              '<button @click="showRepeatedRepair">Показать повторные ремонты</button>' +
            '</div>',
  methods: {
    showAll: function() {
        getByMapping('/atm')
    },
    showTop3Reasons: function() {
        getByMapping('/atm/top3/reason')
    },
    showTop3Time: function() {
        getByMapping('/atm/top3/time')
    },
    showRepeatedRepair: function() {
        getByMapping('/atm/repair/repeated')
    },
    save: function() {
        icon.isShown = true;
        if (grid.deletedRows.size > 0) {
            let list = [];
            grid.deletedRows.forEach((v, v2) => {
              list.push(v);
            });
            this.$http.post('atm/row/delete', JSON.stringify(list),  {
               before(request) {
                 if (this.previousRequest) {
                   this.previousRequest.abort();
                 }
                 this.previousRequest = request;
               }
            }).then(response => {
                icon.isShown = false;
            }, response => {
                alert('Ошибка при загрузке !');
                this.tableData = response.body
                icon.isShown = false;
            });
            grid.deletedRows.clear();
        }

        if (grid.editedRows.size > 0) {
            let list = [];
            grid.editedRows.forEach((v, v2) => {
              list.push(v);
            });
            this.$http.put('atm/row/edit', JSON.stringify(list), {
               before(request) {
                 if (this.previousRequest) {
                   this.previousRequest.abort();
                 }
                 this.previousRequest = request;
               }
            }).then(response => {
                icon.isShown = false;
            }, response => {
                alert('Ошибка при загрузке !');
                this.tableData = response.body
                icon.isShown = false;
            });
            grid.editedRows.clear();
        }
        icon.isShown = false;
    },
  },
});


function getByMapping(mapping) {
     icon.isShown = true;
     Vue.http.get(mapping, {
        before(request) {
          if (this.previousRequest) {
            this.previousRequest.abort();
          }
          this.previousRequest = request;
        }
     }).then(response => {
         grid.tableData = response.body
         icon.isShown = false;
     }, response => {
         grid.tableData = [{reason: 'Ошибка при загрузке !'}];
         icon.isShown = false;
     });
}
