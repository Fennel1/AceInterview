<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()">
      <el-row :gutter="20">
        <el-col :span="5">
          <el-form-item label="试卷标题：" prop="title">
            <el-input v-model="dataForm.title" placeholder="试卷标题" style="width: 140px;"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="5">
          <el-form-item label="试卷限时：" prop="limitTime">
            <el-input v-model="dataForm.limitTime" placeholder="试卷限时" style="width: 90px;"></el-input>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item label="难度：" prop="level">
          <el-radio-group v-model="dataForm.level">
            <el-radio :label=0>简单</el-radio>
            <el-radio :label=1>中等</el-radio>
            <el-radio :label=2>最难</el-radio>
          </el-radio-group>
        </el-form-item>
        </el-col>
        
        <el-col :span="5">
          <el-form-item label="是否开启：" prop="enable">
            <el-switch
              v-model="dataForm.enable"
              :active-value="1"
              :inactive-value="0"
              active-color="#13ce66"
              inactive-color="#ff4949"
            ></el-switch>
      </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-row :gutter="20" style="display: flex;">
      <el-col :span="20" style="width: 40rem;">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>题库</span>
          </div>
        <!-- 选择题目-->
          <el-form :inline="true" :model="dataForm" @keyup.enter.native="getLeftDataList()">
            <el-form-item>
              <el-input v-model="dataForm.leftKey" placeholder="题目标题" clearable></el-input>
            </el-form-item>
            <el-form-item>
              <el-select v-model="leftSelectType" placeholder="请选择题目类型" prop="type">
                <el-option v-for="item in types"
                  :key="item.id"
                  :label="item.comments"
                  :value="item.id*1">
                  <span style="float: left">{{ item.comments }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ item.type }}</span>
                </el-option>
              </el-select>
              <el-button @click="getLeftDataList()">查询</el-button>
              <el-button @click="addQuestionHandle()" type="primary" :disabled="leftDataListSelections.length <= 0">添加</el-button>
            </el-form-item>
          </el-form>
          <el-table
            :data="leftDataList"
            border
            v-loading="leftDataListLoading"
            @selection-change="leftSelectionChangeHandle"
            style="width: 100%;">
            <el-table-column
              type="selection"
              header-align="center"
              align="center"
              width="50">
            </el-table-column>
            <el-table-column
              prop="id"
              header-align="center"
              align="center"
              label="id">
            </el-table-column>
            <el-table-column
              prop="question"
              header-align="center"
              align="center"
              label="问题">
            </el-table-column>
            <el-table-column
              prop="typeComments"
              header-align="center"
              align="center"
              label="题目类型">
            </el-table-column>
          </el-table>
          <el-pagination
            @size-change="leftSizeChangeHandle"
            @current-change="leftCurrentChangeHandle"
            :current-page="leftPageIndex"
            :page-sizes="[5]"
            :page-size="leftPageSize"
            :total="leftTotalPage"
            layout="total, sizes, prev, pager, next, jumper">
          </el-pagination>
        </el-card>
      </el-col>

      <el-col :span="20" style="width: 40rem;">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
              <span>已选题目</span>
          </div>
          <!-- 选择题目-->
          <el-form :inline="true" :model="dataForm" @keyup.enter.native="getRightDataList()">
            <el-form-item>
                <el-input v-model="dataForm.rightKey" placeholder="题目标题" clearable></el-input>
            </el-form-item>
            <el-form-item>
              <el-select v-model="rightSelectType" placeholder="请选择题目类型" prop="type">
                <el-option v-for="item in types"
                  :key="item.id"
                  :label="item.comments"
                  :value="item.id*1">
                  <span style="float: left">{{ item.comments }}</span>
                  <span style="float: right; color: #8492a6; font-size: 13px">{{ item.type }}</span>
                </el-option>
              </el-select>
              <el-button @click="getRightDataList()">查询</el-button>
              <el-button @click="removeQuestionHandle()" type="danger" :disabled="rightDataListSelections.length <= 0">删除</el-button>
            </el-form-item>
          </el-form>
          <el-table
            :data="this.rightDataList"
            height="250"
            border
            v-loading="rightDataListLoading"
            @selection-change="rightSelectionChangeHandle"
            style="width: 100%;">
            <el-table-column
              type="selection"
              header-align="center"
              align="center"
              width="50">
            </el-table-column>
            <el-table-column
              prop="id"
              header-align="center"
              align="center"
              label="id">
            </el-table-column>
            <el-table-column
              prop="question"
              header-align="center"
              align="center"
              label="问题">
            </el-table-column>
            <el-table-column
              prop="typeComments"
              header-align="center"
              align="center"
              label="题目类型">
            </el-table-column>
          </el-table>
          <!-- <el-pagination
            @size-change="rightSizeChangeHandle"
            @current-change="rightCurrentChangeHandle"
            :current-page="rightPageIndex"
            :page-sizes="[5,10]"
            :page-size="rightPageSize"
            :total="rightTotalPage"
            layout="total, sizes, prev, pager, next, jumper">
          </el-pagination> -->
        </el-card>
      </el-col>
    </el-row>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>

</template>

<style>
  .el-dialog {
    width: 75%;
  }
  .el-select {
    width: 130px;
  }
  
  .input-with-select .el-input-group__prepend {
    background-color: #fff;
  }
  .text {
    font-size: 14px;
  }

  .item {
    margin-bottom: 18px;
  }

  .clearfix:before,
  .clearfix:after {
    display: table;
    content: "";
  }
  .clearfix:after {
    clear: both
  }

</style>

<script>
  export default {
    data () {
      return {
        rightDataList: [],
        leftDataListSelections: [],
        rightDataListSelections: [],
        multipleSelection: [],
        visible: false,
        selectedQuestions: [],
        types: [],
        leftDataList: [],
        leftPageIndex: 1,
        leftPageSize: 5,
        leftTotalPage: 0,
        rightPageIndex: 1,
        rightPageSize: 5,
        rightTotalPage: 0,
        leftDataListLoading: false,
        rightDataListLoading: false,
        leftSelectType: '',
        rightSelectType: '',
        dataForm: {
          leftKey: '',
          rightKey: '',
          id: 0,
          title: '',
          limitTime: '',
          level: '',
          enable: '',
          createTime: '',
          updateTime: '',
          delFlag: '',
          createUser: '',
          updateUser: ''
        },
        dataRule: {
          title: [
            { required: true, message: '试卷标题不能为空', trigger: 'blur' }
          ],
          limitTime: [
            { required: true, message: '试卷限时不能为空', trigger: 'blur' }
          ],
          level: [
            { required: true, message: '试卷难度等级(1-简单，2-中等，3-最难）不能为空', trigger: 'blur' }
          ],
          enable: [
            { required: true, message: '是否开启不能为空', trigger: 'blur' }
          ],
        }
      }
    },
    activated () {
      this.getLeftDataList()
      this.getRightDataList()
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$http({
            url: this.$http.adornUrl(
              `/question/type/list`
            ),
            method: "get",
            params: this.$http.adornParams()
          }).then(({ data }) => {
            if (data && data.code === 0) {
              this.types = data.page.list;
        }});
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/question/exam/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.title = data.exam.title
                this.dataForm.limitTime = data.exam.limitTime
                this.dataForm.level = data.exam.level
                this.dataForm.enable = data.exam.enable
                this.dataForm.createTime = data.exam.createTime
                this.dataForm.updateTime = data.exam.updateTime
                this.dataForm.delFlag = data.exam.delFlag
                this.dataForm.createUser = data.exam.createUser
                this.dataForm.updateUser = data.exam.updateUser
              }
            })
          }
        });
        this.getLeftDataList();
        this.getRightDataList();
      },
      // 获取数据列表
      getLeftDataList () {
        this.leftDataListLoading = true
        this.$http({
          url: this.$http.adornUrl('/question/examquestion/list2'),
          method: 'get',
          params: this.$http.adornParams({
            current: this.leftPageIndex,
            limit: this.leftPageSize,
            size: this.leftPageSize,
            key: this.dataForm.leftKey,
            type: this.leftSelectType
          })
        }).then(({data}) => {
          if (data && data.code === 0) {
            const translatedRecords = data.page.records.map((record) => {
              return {
                ...record,
                multiple: record.multiple === 0 ? '单选' : '多选'
              };
            });
            this.leftDataList = translatedRecords;
            this.leftTotalPage = data.page.total;
          } else {
            this.leftDataList = [];
            this.leftTotalPage = 0;
          }
          this.leftDataListLoading = false;
        });
      },
      // 获取数据列表
      getRightDataList () {
        this.rightDataListLoading = true
        this.$http({
          url: this.$http.adornUrl(`/question/exam/questions/${this.dataForm.id}`),
          method: 'get',
          params: this.$http.adornParams({
            current: this.leftPageIndex,
            limit: this.leftPageSize,
            key: this.dataForm.rightKey,
            type: this.rightSelectType
          })
        }).then(({data}) => {
          if (data && data.code === 0) {
            this.rightDataList = data.records;
            //this.rightTotalPage = data.total;
          } else {
            this.rightDataList = [];
            //this.rightTotalPage = 0;
          }
          this.rightDataListLoading = false;
        });
      },
      // 每页数
      leftSizeChangeHandle (val) {
        this.leftPageSize = val
        this.leftPageIndex = 1
        this.getLeftDataList()
      },
      // 当前页
      leftCurrentChangeHandle (val) {
        this.leftPageIndex = val
        this.getLeftDataList()
      },
      // 每页数
      rightSizeChangeHandle (val) {
        this.leftPageSize = val
        this.leftPageIndex = 1
        this.getLeftDataList()
      },
      // 当前页
      rightCurrentChangeHandle (val) {
        this.leftPageIndex = val
        this.getLeftDataList()
      },
      // 左侧多选
      leftSelectionChangeHandle (val) {
        this.leftDataListSelections = val
        console.log(val)
      },
      // 右侧多选
      rightSelectionChangeHandle (val) {
        this.rightDataListSelections = val
        console.log(val)
      },
      addQuestionHandle() {
        this.leftDataListSelections.forEach(item => {
          // 根据ID或名称条件进行过滤
          let shouldAdd = !this.rightDataList.some(existingItem => existingItem.id === item.id);
          if (shouldAdd) {
            this.rightDataList.push(item);
          }
        });
      },
      removeQuestionHandle() {
        this.rightDataList = this.rightDataList.filter(item => {
          // 假设item具有id属性
          return !this.rightDataListSelections.some(selectedItem => selectedItem.id === item.id);
        });
      },

      // 表单提交
      dataFormSubmit () {
        // 处理选中的题目
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            const questionIdList = this.rightDataList.map(item => item.id);
            console.log(questionIdList);
            this.$http({
              url: this.$http.adornUrl(`/question/exam/${!this.dataForm.id ? 'save' : 'update-detail'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'title': this.dataForm.title,
                'limitTime': this.dataForm.limitTime,
                'level': this.dataForm.level,
                'enable': this.dataForm.enable,
                'createTime': this.dataForm.createTime,
                'updateTime': this.dataForm.updateTime,
                'delFlag': this.dataForm.delFlag,
                'createUser': this.dataForm.createUser,
                'updateUser': this.dataForm.updateUser,
                'questionIdList': questionIdList
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      },
      // 删除
      deleteHandle (id) {
        var ids = id ? [id] : this.leftDataListSelections.map(item => {
          return item.id
        })
        this.$confirm(`确定对[id=${ids.join(',')}]进行[${id ? '删除' : '批量删除'}]操作?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl(''),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            if (data && data.code === 0) {
              this.$message({
                message: '操作成功',
                type: 'success',
                duration: 1500,
                onClose: () => {
                  this.getDataList()
                }
              })
            } else {
              this.$message.error(data.msg)
            }
          })
        })
      }
    }
  }
</script>
