<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()">
    <el-form-item label="问题" prop="question">
      <el-input v-model="dataForm.question" placeholder="问题"></el-input>
    </el-form-item>
    <el-form-item label="选项 A" prop="chooseA">
      <el-input  v-model="dataForm.chooseA" placeholder="答案选项 A"></el-input>
    </el-form-item>
    <el-form-item label="选项 B" prop="chooseB">
      <el-input v-model="dataForm.chooseB" placeholder="答案选项 B"></el-input>
    </el-form-item>
    <el-form-item label="选项 C" prop="chooseC">
      <el-input v-model="dataForm.chooseC" placeholder="答案选项 C"></el-input>
    </el-form-item>
    <el-form-item label="选项 D" prop="chooseD">
      <el-input v-model="dataForm.chooseD" placeholder="答案选项 D"></el-input>
    </el-form-item>
    <el-form-item label="正确选项" prop="rightChooseList">
      <el-checkbox-group v-model="dataForm.rightChooseList">
        <el-checkbox label="A"></el-checkbox>
        <el-checkbox label="B"></el-checkbox>
        <el-checkbox label="C"></el-checkbox>
        <el-checkbox label="D"></el-checkbox>
      </el-checkbox-group>
    </el-form-item>
    <el-form-item label="题目类型" prop="type">
      <el-select v-model="dataForm.type" placeholder="请选择" @change="chooseType">
        <el-option v-for="item in types" 
          :key="item.id" 
          :label="item.comments" 
          :value="item.id*1">
          <span style="float: left">{{ item.comments }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">{{ item.type }}</span>
        </el-option>
      </el-select>
    </el-form-item>
    <el-form-item label="是否多选" prop="multiple">
      <el-radio-group v-model="dataForm.multiple">
        <el-radio :label=0>单选</el-radio>
        <el-radio :label=1>多选</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item label="是否开启" prop="enable">
        <el-switch
          v-model="dataForm.enable"
          :active-value="1"
          :inactive-value="0"
          active-color="#13ce66"
          inactive-color="#ff4949"
        ></el-switch>
      </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        types: [],
        dataForm: {
          id: 0,
          question: '',
          chooseA: '',
          chooseB: '',
          chooseC: '',
          chooseD: '',
          rightChoose: '',
          rightChooseList: [],
          type: '',
          multiple: 0,
          createTime: '',
          updateTime: '',
          delFlag: '',
          createUser: '',
          updateUser: '',
          enable: ''
        },
        dataRule: {
          question: [
            { required: true, message: '问题不能为空', trigger: 'blur' }
          ],
          chooseA: [
            { required: true, message: '答案选项 A不能为空', trigger: 'blur' }
          ],
          chooseB: [
            { required: true, message: '答案选项 B不能为空', trigger: 'blur' }
          ],
          chooseC: [
            { required: true, message: '答案选项 C不能为空', trigger: 'blur' }
          ],
          chooseD: [
            { required: true, message: '答案选项 D不能为空', trigger: 'blur' }
          ],
          rightChoose: [
            { required: true, message: '正确选项不能为空', trigger: 'blur' }
          ],
          type: [
            { required: true, message: '题目类型不能为空', trigger: 'blur' }
          ],
          multiple: [
            { required: true, message: '是否多选不能为空', trigger: 'blur' }
          ],
          enable: [
            { required: true, message: '是否开启不能为空', trigger: 'blur' }
          ]
        }
      }
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
              url: this.$http.adornUrl(`/question/examquestion/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.question = data.examQuestion.question
                this.dataForm.chooseA = data.examQuestion.chooseA
                this.dataForm.chooseB = data.examQuestion.chooseB
                this.dataForm.chooseC = data.examQuestion.chooseC
                this.dataForm.chooseD = data.examQuestion.chooseD
                this.dataForm.rightChoose = data.examQuestion.rightChoose
                this.dataForm.rightChooseList = data.examQuestion.rightChooseList
                this.dataForm.type = data.examQuestion.type
                this.dataForm.multiple = data.examQuestion.multiple
                this.dataForm.enable = data.examQuestion.enable
              }
            })
          }
        })
      },
      chooseType(e) {
        console.log(e);
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/question/examquestion/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'question': this.dataForm.question,
                'chooseA': this.dataForm.chooseA,
                'chooseB': this.dataForm.chooseB,
                'chooseC': this.dataForm.chooseC,
                'chooseD': this.dataForm.chooseD,
                'rightChoose': this.dataForm.rightChooseList.sort().join(","),
                'type': this.dataForm.type,
                'multiple': this.dataForm.multiple,
                'enable': this.dataForm.enable
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
      }
    }
  }
</script>
