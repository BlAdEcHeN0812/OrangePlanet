const { createApp, ref, reactive, onMounted, computed } = Vue;
const { ElMessage } = ElementPlus;

// Ensure ElementPlusIconsVue is available
const Icons = typeof ElementPlusIconsVue !== 'undefined' ? ElementPlusIconsVue : {};
const { User, Lock } = Icons;

if (Object.keys(Icons).length === 0) {
    console.error("Element Plus Icons not loaded! Check network or CDN.");
}

const app = createApp({
    setup() {
        const API_BASE = '/api';
        const isLoggedIn = ref(false);
        const loading = ref(false);
        const courses = ref([]);
        const currentCourse = ref(null);
        
        const loginForm = reactive({
            username: '',
            password: ''
        });

        const timeSlots = [
            { id: 1, time: '08:00' }, { id: 2, time: '08:50' },
            { id: 3, time: '10:00' }, { id: 4, time: '10:50' },
            { id: 5, time: '11:40' }, { id: 6, time: '13:25' },
            { id: 7, time: '14:15' }, { id: 8, time: '15:05' },
            { id: 9, time: '16:15' }, { id: 10, time: '17:05' },
            { id: 11, time: '18:50' }, { id: 12, time: '19:40' },
            { id: 13, time: '20:30' }
        ];

        const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];

        // Process courses to split multi-period courses into single cells
        const processedCourses = computed(() => {
            const result = [];
            if (!courses.value) return result;
            
            courses.value.forEach(course => {
                const dayIndex = parseDay(course.dayOfWeek);
                const startRow = parseInt(course.startTime);
                const duration = parseInt(course.periodCount || course.PeriodCount || 1);

                if (dayIndex > 0 && startRow > 0) {
                    for (let i = 0; i < duration; i++) {
                        result.push({
                            ...course,
                            gridRow: startRow + i,
                            gridCol: dayIndex
                        });
                    }
                }
            });
            return result;
        });

        const checkBackendAndLoad = async () => {
            try {
                const response = await fetch(`${API_BASE}/my-courses`);
                if (response.ok) {
                    const data = await response.json();
                    if (data && data.length > 0) {
                        courses.value = data;
                        isLoggedIn.value = true;
                    } else {
                        isLoggedIn.value = false;
                    }
                } else {
                    isLoggedIn.value = false;
                }
            } catch (e) {
                console.log("Backend not ready yet, retrying...");
                // Don't retry indefinitely to avoid console spam if server is down
                // setTimeout(checkBackendAndLoad, 2000);
            }
        };

        const handleLogin = async () => {
            if (!loginForm.username || !loginForm.password) {
                ElMessage.warning('请输入学号和密码');
                return;
            }

            loading.value = true;
            try {
                const response = await fetch(`${API_BASE}/courses?username=${encodeURIComponent(loginForm.username)}&password=${encodeURIComponent(loginForm.password)}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    const data = await response.json();
                    console.log("Login success, data:", data);
                    courses.value = data;
                    isLoggedIn.value = true;
                    ElMessage.success('登录成功');
                } else {
                    ElMessage.error('登录失败，请检查账号密码');
                }
            } catch (e) {
                console.error("Login error:", e);
                ElMessage.error('连接服务器出错，请稍后重试');
            } finally {
                loading.value = false;
            }
        };

        const logout = () => {
            isLoggedIn.value = false;
            courses.value = [];
            currentCourse.value = null;
            loginForm.password = '';
        };

        const showDetails = (course) => {
            currentCourse.value = course;
        };

        const parseDay = (dayStr) => {
            const map = {
                '星期一': 1, 'Mon': 1,
                '星期二': 2, 'Tue': 2,
                '星期三': 3, 'Wed': 3,
                '星期四': 4, 'Thu': 4,
                '星期五': 5, 'Fri': 5,
                '星期六': 6, 'Sat': 6,
                '星期日': 7, 'Sun': 7
            };
            return map[dayStr] || 0;
        };

        const getCourseStyle = (course) => {
            const colors = ['#e3f2fd', '#e8f5e9', '#fff3e0', '#f3e5f5', '#e0f7fa', '#fce4ec', '#f1f8e9'];
            let hash = 0;
            const str = course.name || '';
            for (let i = 0; i < str.length; i++) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            const colorIndex = Math.abs(hash) % colors.length;
            
            return {
                gridColumn: course.gridCol,
                gridRow: course.gridRow + 1,
                backgroundColor: colors[colorIndex],
                cursor: 'pointer'
            };
        };

        onMounted(() => {
            checkBackendAndLoad();
        });

        return {
            timeSlots,
            days,
            isLoggedIn,
            loginForm,
            loading,
            processedCourses,
            currentCourse,
            handleLogin,
            logout,
            showDetails,
            getCourseStyle,
            User,
            Lock
        };
    }
});

app.use(ElementPlus);
for (const [key, component] of Object.entries(Icons)) {
    app.component(key, component);
}
app.mount('#app');
