const { createApp, ref, reactive, onMounted, computed } = Vue;
const { ElMessage } = ElementPlus;

// Ensure ElementPlusIconsVue is available
const Icons = typeof ElementPlusIconsVue !== 'undefined' ? ElementPlusIconsVue : {};
const { User, Lock, List, Message, Notebook, Calendar, Setting, Close, Delete, Edit, Plus, Picture, ArrowRight } = Icons;

if (Object.keys(Icons).length === 0) {
    console.error("Element Plus Icons not loaded! Check network or CDN.");
}

const app = createApp({
    setup() {
        const API_BASE = '/api';
        const isLoggedIn = ref(false);
        const loading = ref(false);
        const courses = ref([]);
        const emails = ref([]);
        const todos = ref([]);
        const currentView = ref('courses');
        const currentCourse = ref(null);
        const currentEmail = ref(null);
        const emailDialogVisible = ref(false);
        const emailPassword = ref('');
        
        // Todo related
        const todoDialogVisible = ref(false);
        const currentTodo = reactive({ id: null, title: '', content: '', completed: false });
        
        // Learning related
        const learningCourses = ref([]);
        const learningDialogVisible = ref(false);
        const currentLearningCourse = ref(null);
        const learningActiveTab = ref('uploads');
        const courseUploads = ref([]);
        const courseRollCalls = ref([]);

        const selectedYear = ref('2024-2025');
        const selectedSemester = ref('1|秋');
        
        const yearOptions = [
            { value: '2023-2024', label: '2023-2024' },
            { value: '2024-2025', label: '2024-2025' },
            { value: '2025-2026', label: '2025-2026' }
        ];
        
        const semesterOptions = [
            { value: '1|秋', label: '秋' },
            { value: '1|冬', label: '冬' },
            { value: '2|春', label: '春' },
            { value: '2|夏', label: '夏' },
            { value: '0|短', label: '短学期' }
        ];
        
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
            
            // Extract filter from selectedSemester (e.g. "1|秋" -> "秋")
            let filterKey = '';
            if (selectedSemester.value && selectedSemester.value.includes('|')) {
                filterKey = selectedSemester.value.split('|')[1];
            }

            courses.value.forEach(course => {
                // Filter logic:
                // If filterKey is present, check if course.weeks contains the filterKey
                // OR if course.weeks contains the combined semester name (e.g. "秋冬" contains "秋")
                // Note: "秋冬" contains "秋" and "冬", so it shows for both.
                // "秋" contains "秋", shows for Autumn.
                // "冬" does not contain "秋", hidden for Autumn.
                if (filterKey && course.weeks && !course.weeks.includes(filterKey)) {
                    // Special case: if course.weeks is empty or null, maybe show it? 
                    // But usually it has value.
                    // Also handle "短" for short term
                    return; 
                }

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

        const fetchEmails = async () => {
            loading.value = true;
            try {
                const formData = new FormData();
                formData.append('username', loginForm.username);
                formData.append('password', emailPassword.value || loginForm.password);

                const response = await fetch(`${API_BASE}/emails`, {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    emails.value = await response.json();
                    ElMessage.success('邮件获取成功');
                    if (emailPassword.value) {
                        localStorage.setItem('email_password', emailPassword.value);
                    }
                } else {
                    ElMessage.error('获取邮件失败');
                }
            } catch (error) {
                console.error('Error fetching emails:', error);
                ElMessage.error('网络错误');
            } finally {
                loading.value = false;
            }
        };

        // Todo Functions
        const fetchTodos = async () => {
            console.log('Fetching todos...');
            try {
                const response = await fetch(`${API_BASE}/todos`);
                if (response.ok) {
                    const data = await response.json();
                    console.log('Fetched todos:', data);
                    todos.value = data;
                } else {
                    console.error('Failed to fetch todos:', response.status);
                }
            } catch (error) {
                console.error('Error fetching todos:', error);
            }
        };

        const openTodoDialog = (todo = null) => {
            if (todo) {
                Object.assign(currentTodo, todo);
            } else {
                Object.assign(currentTodo, { id: null, title: '', content: '', completed: false });
            }
            todoDialogVisible.value = true;
        };

        const saveTodo = async () => {
            if (!currentTodo.title) {
                ElMessage.warning('请输入标题');
                return;
            }
            
            try {
                const url = currentTodo.id ? `${API_BASE}/todos/${currentTodo.id}` : `${API_BASE}/todos`;
                const method = currentTodo.id ? 'PUT' : 'POST';
                
                const response = await fetch(url, {
                    method: method,
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(currentTodo)
                });

                if (response.ok) {
                    ElMessage.success('保存成功');
                    todoDialogVisible.value = false;
                    fetchTodos();
                } else {
                    ElMessage.error('保存失败');
                }
            } catch (error) {
                ElMessage.error('网络错误');
            }
        };

        const deleteTodo = async (id) => {
            try {
                await fetch(`${API_BASE}/todos/${id}`, { method: 'DELETE' });
                ElMessage.success('删除成功');
                fetchTodos();
            } catch (error) {
                ElMessage.error('删除失败');
            }
        };

        const updateTodoStatus = async (todo) => {
            try {
                await fetch(`${API_BASE}/todos/${todo.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(todo)
                });
            } catch (error) {
                console.error('Error updating status:', error);
            }
        };

        // Watch view change to fetch todos
        Vue.watch(currentView, (newVal) => {
            if (newVal === 'todos') {
                fetchTodos();
            }
        });

        const handleLogin = async () => {
            if (!loginForm.username || !loginForm.password) {
                ElMessage.warning('请输入学号和密码');
                return;
            }

            loading.value = true;
            try {
                // Extract API code from selectedSemester (e.g. "1|秋" -> "1")
                let apiSemester = selectedSemester.value;
                if (apiSemester && apiSemester.includes('|')) {
                    apiSemester = apiSemester.split('|')[0];
                }

                const response = await fetch(`${API_BASE}/courses?username=${encodeURIComponent(loginForm.username)}&password=${encodeURIComponent(loginForm.password)}&year=${encodeURIComponent(selectedYear.value)}&semester=${encodeURIComponent(apiSemester)}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    const data = await response.json();
                    console.log("Login success, data:", data);
                    courses.value = data;
                    isLoggedIn.value = true;
                    ElMessage.success('登录成功');
                    // Pre-login to Learning in ZJU in background
                    fetch(`${API_BASE}/learning/login?username=${encodeURIComponent(loginForm.username)}&password=${encodeURIComponent(loginForm.password)}`, { method: 'POST' });
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

        const fetchLearningCourses = async () => {
            loading.value = true;
            try {
                // Ensure login
                await fetch(`${API_BASE}/learning/login?username=${encodeURIComponent(loginForm.username)}&password=${encodeURIComponent(loginForm.password)}`, { method: 'POST' });
                
                const response = await fetch(`${API_BASE}/learning/courses`);
                const data = await response.json();
                if (data && data.courses) {
                    // Filter based on selected year and semester
                    // Format of course_code: (2024-2025-1)-...
                    const year = selectedYear.value;
                    let semester = selectedSemester.value;
                    if (semester && semester.includes('|')) {
                        semester = semester.split('|')[0];
                    }
                    
                    const prefix = `(${year}-${semester})`;
                    console.log("Filtering courses with prefix:", prefix);
                    
                    learningCourses.value = data.courses.filter(course => {
                        return course.course_code && course.course_code.startsWith(prefix);
                    }).map((/** @type {any} */ course) => {
                        console.log("Processing course:", course.name);
                        console.log("Full course object:", JSON.stringify(course));
                        
                        // Check for small_cover
                        if (course.small_cover) {
                            console.log("Found small_cover:", course.small_cover);
                            course.cover = course.small_cover;
                        } else {
                            console.log("No small_cover found");
                        }

                        if (!course.cover) {
                            course.cover = 'https://courses.zju.edu.cn/static/assets/images/large/74db89b7f92df4c9f372.png';
                        }
                        return course;
                    });
                } else {
                    learningCourses.value = [];
                }
            } catch (error) {
                console.error('Failed to fetch learning courses:', error);
                ElMessage.error('获取课程失败');
            } finally {
                loading.value = false;
            }
        };

        const showLearningDetails = (course) => {
            currentLearningCourse.value = course;
            learningDialogVisible.value = true;
            learningActiveTab.value = 'uploads';
            courseUploads.value = [];
            courseRollCalls.value = [];
            fetchCourseUploads(course.id);
        };

        const fetchCourseUploads = async (courseId) => {
            try {
                const response = await fetch(`${API_BASE}/learning/courses/${courseId}/uploads`);
                const data = await response.json();
                
                let activities = [];
                if (Array.isArray(data)) {
                    activities = data;
                } else if (data && data.activities) {
                    activities = data.activities;
                }

                // Flatten the structure: extract uploads from activities
                const extractedFiles = [];
                activities.forEach(activity => {
                    // Case 1: Activity has uploads array
                    if (activity.uploads && activity.uploads.length > 0) {
                        activity.uploads.forEach(upload => {
                            let fileUrl = upload.url;
                            if (!fileUrl) {
                                // Fallback construction
                                if (upload.reference_id) {
                                    fileUrl = `https://courses.zju.edu.cn/api/uploads/reference/${upload.reference_id}/blob`;
                                } else if (upload.id) {
                                    fileUrl = `https://courses.zju.edu.cn/api/uploads/${upload.id}/blob`;
                                }
                            }
                            
                            extractedFiles.push({
                                id: upload.id,
                                title: upload.file_name || activity.title,
                                url: fileUrl,
                                file_name: upload.file_name
                            });
                        });
                    } 
                });

                courseUploads.value = extractedFiles;
                
            } catch (error) {
                console.error('Failed to fetch uploads:', error);
            }
        };

        const fetchRollCalls = async (courseId) => {
            try {
                const response = await fetch(`${API_BASE}/learning/courses/${courseId}/rollcalls`);
                const data = await response.json();
                console.log("Rollcall raw data:", data);

                let activities = [];
                if (Array.isArray(data)) {
                    activities = data;
                } else if (data && data.activities) {
                    activities = data.activities;
                } else if (data && data.rollcalls) {
                    activities = data.rollcalls;
                }

                // Filter for roll calls and deduplicate
                const seenIds = new Set();
                courseRollCalls.value = activities.filter(a => {
                    // Filter by type if present (activities list items have type, radar items might not)
                    // Allow 'another' as it is returned by the modules/rollcalls endpoint
                    if (a.type && a.type !== 'rollcall' && a.type !== 'classroom_sign_in' && a.type !== 'another') {
                        return false;
                    }

                    // Filter by course_id
                    if (a.course_id && String(a.course_id) !== String(courseId)) {
                        return false;
                    }
                    
                    // Deduplicate
                    if (a.id && seenIds.has(a.id)) {
                        return false;
                    }
                    if (a.id) seenIds.add(a.id);
                    
                    return true;
                });
                
            } catch (error) {
                console.error('Failed to fetch roll calls:', error);
                ElMessage.error('获取点名失败');
            }
        };
        
        const getProxyUrl = (url) => {
            return `${API_BASE}/learning/file/proxy?url=${encodeURIComponent(url)}`;
        };

        const downloadFile = (url) => {
            const proxyUrl = getProxyUrl(url);
            // Create a temporary link to trigger download
            const link = document.createElement('a');
            link.href = proxyUrl;
            link.download = ''; // Browser should infer filename from Content-Disposition
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        };

        const previewFile = (url) => {
            const proxyUrl = getProxyUrl(url);
            window.open(proxyUrl, '_blank');
        };

        const refreshCourses = async () => {
             if (!loginForm.username || !loginForm.password) {
                ElMessage.warning('请先重新登录以获取密码');
                return;
            }
            
            loading.value = true;
            try {
                // Extract API code from selectedSemester (e.g. "1|秋" -> "1")
                let apiSemester = selectedSemester.value;
                if (apiSemester && apiSemester.includes('|')) {
                    apiSemester = apiSemester.split('|')[0];
                }

                const response = await fetch(`${API_BASE}/courses?username=${encodeURIComponent(loginForm.username)}&password=${encodeURIComponent(loginForm.password)}&year=${encodeURIComponent(selectedYear.value)}&semester=${encodeURIComponent(apiSemester)}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    const data = await response.json();
                    courses.value = data;
                    ElMessage.success('查询成功');
                } else {
                    ElMessage.error('查询失败');
                }
            } catch (e) {
                ElMessage.error('网络错误');
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

        const showEmailDetails = (email) => {
            currentEmail.value = email;
            emailDialogVisible.value = true;
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

        const getCourseCoverStyle = (course) => {
            const gradients = [
                'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                'linear-gradient(120deg, #84fab0 0%, #8fd3f4 100%)',
                'linear-gradient(120deg, #fccb90 0%, #d57eeb 100%)',
                'linear-gradient(120deg, #e0c3fc 0%, #8ec5fc 100%)',
                'linear-gradient(120deg, #f093fb 0%, #f5576c 100%)',
                'linear-gradient(120deg, #89f7fe 0%, #66a6ff 100%)'
            ];
            
            let hash = 0;
            const str = course.name || '';
            for (let i = 0; i < str.length; i++) {
                hash = str.charCodeAt(i) + ((hash << 5) - hash);
            }
            const index = Math.abs(hash) % gradients.length;
            
            return {
                background: gradients[index]
            };
        };

        const formatDate = (dateStr) => {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            return date.toLocaleString('zh-CN', { hour12: false });
        };

        const formatRollCallTime = (dateStr) => {
            if (!dateStr) return '';
            const date = new Date(dateStr);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            const day = String(date.getDate()).padStart(2, '0');
            const hours = String(date.getHours()).padStart(2, '0');
            const minutes = String(date.getMinutes()).padStart(2, '0');
            return `${year}.${month}.${day} ${hours}:${minutes}`;
        };

        const getRollCallType = (rc) => {
            if (rc.source === 'number' || rc.is_number) return '数字点名';
            if (rc.source === 'radar' || rc.is_radar) return '雷达点名';
            if (rc.source === 'qrcode') return '二维码点名';
            return '点名';
        };

        onMounted(() => {
            checkBackendAndLoad();
            const savedPass = localStorage.getItem('email_password');
            if (savedPass) {
                emailPassword.value = savedPass;
            }
        });

        return {
            timeSlots,
            days,
            isLoggedIn,
            loginForm,
            loading,
            processedCourses,
            currentCourse,
            currentEmail,
            emailDialogVisible,
            emailPassword,
            handleLogin,
            logout,
            showDetails,
            showEmailDetails,
            getCourseStyle,
            getCourseCoverStyle,
            formatDate,
            User,
            Lock,
            selectedYear,
            selectedSemester,
            yearOptions,
            semesterOptions,
            refreshCourses,
            fetchEmails,
            emails,
            currentView,
            List,
            Message,
            Notebook,
            Calendar,
            Setting,
            Close,
            Delete,
            Edit,
            Plus,
            todos,
            todoDialogVisible,
            currentTodo,
            openTodoDialog,
            saveTodo,
            deleteTodo,
            updateTodoStatus,
            
            // Learning
            learningCourses,
            learningDialogVisible,
            currentLearningCourse,
            learningActiveTab,
            courseUploads,
            courseRollCalls,
            fetchLearningCourses,
            showLearningDetails,
            fetchRollCalls,
            downloadFile,
            previewFile,
            Picture,
            ArrowRight,
            formatRollCallTime,
            getRollCallType
        };
    }
});

app.use(ElementPlus);
for (const [key, component] of Object.entries(Icons)) {
    app.component(key, component);
}
app.mount('#app');
