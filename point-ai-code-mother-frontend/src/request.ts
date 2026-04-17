import axios from 'axios'
import { message } from 'ant-design-vue'

const myAxios = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 60000,
  withCredentials: true,
})

myAxios.interceptors.request.use(
  function (config) {
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    if (data?.code === 40100 && !response.request.responseURL.includes('user/get/login')) {
      message.warning('请先登录')
    }
    return response
  },
  function (error) {
    return Promise.reject(error)
  },
)

export default myAxios
