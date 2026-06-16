import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import './style.css'
import axios from "axios";
import { initTokenAutoRefresh } from './utils/request'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
// 引入AI客服全局组件
import AiChatService from './components/AiChatService.vue'
const app = createApp(App)

// 注册Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}
// 挂载中文国际化
app.use(ElementPlus, {
    locale: zhCn
})
// 挂载AI客服全局组件
app.component('AiChatService', AiChatService)

// 配置后端API基础路径（指定8084端口）
axios.defaults.baseURL = 'http://localhost:8080';
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
initTokenAutoRefresh()
app.mount('#app')