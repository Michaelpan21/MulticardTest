

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
        grid.tableData = [{id: 'Загрузка..'}]
        this.$http.get('/atm').then(response => {
            grid.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });
    },
    showTop3Reasons: function() {
        grid.tableData = [{id: 'Загрузка..'}]
        this.$http.get('/atm/top3/reason').then(response => {
            grid.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });
    },
    showTop3Time: function() {
        grid.tableData = [{id: 'Загрузка..'}]
        this.$http.get('/atm/top3/time').then(response => {
            grid.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });
    },
    showRepeatedRepair: function() {
        grid.tableData = [{id: 'Загрузка..'}]
        this.$http.get('/atm/repair/repeated').then(response => {
            grid.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });
    },
    save: function() {
        grid.tableData = [{id: 'Загрузка..'}]
        this.$http.put('/atm').then(response => {
            grid.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });
    },
  },
});

var grid = new Vue({
    el: '#app',
    data: {
      tableData: [{id: 'Загрузка..'}],
    },
    mounted: function() {
        /*this.$http.get('/atm').then(response => {
            this.tableData = response.body
          }, response => {
          grid.tableData = [{id: 'Ошибка при загрузке !'}]
        });*/
    },
    methods: {
      deleteRow: function(index, rows) {
        rows.splice(index, 1);
      }
    },
});


