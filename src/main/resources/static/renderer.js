const API_BASE = '/api';

//时间段配置
const timeSlots = [
    { id: 1, time: '08:00' }, { id: 2, time: '08:50' },
    { id: 3, time: '10:00' }, { id: 4, time: '10:50' },
    { id: 5, time: '11:40' }, { id: 6, time: '13:25' },
    { id: 7, time: '14:15' }, { id: 8, time: '15:05' },
    { id: 9, time: '16:15' }, { id: 10, time: '17:05' },
    { id: 11, time: '18:50' }, { id: 12, time: '19:40' },
    { id: 13, time: '20:30' }
];

// UI初始化
window.onload = () => {
    renderGrid();
    checkBackendAndLoad();
};

// 渲染时间表网格
function renderGrid() {
    const timeSlotsContainer = document.getElementById('time-slots');
    const gridContainer = document.getElementById('timetable-grid');

    // 渲染时间列
    timeSlots.forEach(slot => {
        const div = document.createElement('div');
        div.className = 'time-slot';
        div.innerHTML = `<span>${slot.id}</span><span>${slot.time}</span>`;
        timeSlotsContainer.appendChild(div);
    });

    // 渲染网格单元格
    for (let row = 1; row <= 13; row++) {
        for (let col = 1; col <= 7; col++) {
            const cell = document.createElement('div');
            cell.className = 'grid-cell';
            cell.dataset.row = row;
            cell.dataset.col = col;
            // Grid layout positioning
            cell.style.gridRow = row + 1; // +1 for header
            cell.style.gridColumn = col;
            gridContainer.appendChild(cell);
        }
    }
}

// 检查后端状态并加载课程
async function checkBackendAndLoad() {
    try {
        // 尝试获取已有课程
        const response = await fetch(`${API_BASE}/my-courses`);
        if (response.ok) {
            const courses = await response.json();
            if (courses && courses.length > 0) {
                hideLogin();    //调用隐藏登录界面
                renderCourses(courses); //渲染课程
            } else {
                // 没有课程，显示登录界面
                showLogin();
            }
        } else {
            showLogin();
        }
    } catch (e) {
        console.log("Backend not ready yet, retrying...");
        setTimeout(checkBackendAndLoad, 2000);
    }
}

// 显示登录界面
function showLogin() {
    document.getElementById('login-overlay').style.display = 'flex';
}
// 隐藏登录界面
function hideLogin() {
    document.getElementById('login-overlay').style.display = 'none';
}

// 处理登录
async function handleLogin() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const msg = document.getElementById('login-msg');

    msg.innerText = "登录中...";
    
    try {
        const response = await fetch(`${API_BASE}/courses?username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`, {
            method: 'POST'
        });

        if (response.ok) {
            const courses = await response.json();
            hideLogin();                //隐藏登录界面
            renderCourses(courses);     //渲染课程
        } else {
            msg.innerText = "登录失败，请检查账号密码。";
        }
    } catch (e) {
        msg.innerText = "连接服务器出错，请稍后重试。";
    }
}

function refreshCourses() {
    showLogin();
}
// 渲染课程时间表
function renderCourses(courses) {
    // 清除现有课程卡片
    document.querySelectorAll('.course-card').forEach(el => el.remove());

    console.log("Rendering courses:", courses); // Debug log

    if (courses.length > 0) {
        const firstCourse = courses[0];
        console.log("First course sample:", firstCourse);
        // 检查必填字段
        if (!firstCourse.dayOfWeek || !firstCourse.startTime) {
            document.getElementById('course-detail-content').innerHTML = `
                <div style="color: red; padding: 10px;">
                    <strong>数据错误:</strong><br>
                    缺少必填字段。<br>
                    示例数据:<br>
                    <pre>${JSON.stringify(firstCourse, null, 2)}</pre>
                </div>
            `;
        }
    }

    courses.forEach(course => {
        const dayIndex = parseDay(course.dayOfWeek);
        const startRow = parseInt(course.startTime);

        const duration = parseInt(course.periodCount || course.PeriodCount || 1);

        console.log(`Processing course: ${course.name}, Day: ${course.dayOfWeek} (${dayIndex}), Start: ${course.startTime} (${startRow})`);

        if (dayIndex > 0 && startRow > 0) {
            // 将多节课拆分为单独的卡片，使每门课都局限在一个格子中
            for (let i = 0; i < duration; i++) {
                createCourseCard(course, dayIndex, startRow + i, 1);
            }
        }
    });
}

function parseDay(dayStr) {
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
}

// 创建课程卡片并添加到网格
function createCourseCard(course, col, row, span) {
    const grid = document.getElementById('timetable-grid');
    const card = document.createElement('div');
    card.className = 'course-card';
    card.innerHTML = `<strong>${course.name || course.kcmc}</strong><br>${course.location || ''}`;
    
    // 设置卡片位置和大小
    card.style.gridColumn = col;
    card.style.gridRow = `${row + 1} / span ${span}`; // +1 for header
    
    // 颜色编码（可选：随机或基于ID）
    const colors = ['#e3f2fd', '#e8f5e9', '#fff3e0', '#f3e5f5', '#e0f7fa'];
    const colorIndex = (course.id || '').charCodeAt(course.id.length - 1) % colors.length;
    card.style.backgroundColor = colors[colorIndex];

    card.onclick = () => showDetails(course);
    
    grid.appendChild(card);
}

// 显示课程详情 - 简化版 (不需要额外CSS)
function showDetails(course) {
    const container = document.getElementById('course-detail-content');
    
    // 提取单双周信息
    let weekType = '';
    if (course.weeks) {
        if (course.weeks.includes('单周')) {
            weekType = '单周';
        } else if (course.weeks.includes('双周')) {
            weekType = '双周';
        } else if (course.weeks.includes('节/周')) {
            weekType = '每周';
        }
    }
    
    // 提取周次范围
    let weekRange = '';
    if (course.weeks) {
        const match = course.weeks.match(/第([\d\-,]+)周/);
        if (match) {
            weekRange = match[1];
        }
    }
    
    // 提取季节
    let season = '';
    if (course.weeks) {
        const seasonMatch = course.weeks.match(/^(春|夏|秋|冬|春夏|秋冬)/);
        if (seasonMatch) {
            season = seasonMatch[1];
        }
    }
    
    const startTime = parseInt(course.startTime || 1);
    const duration = parseInt(course.periodCount || course.PeriodCount || 1);
    const endTime = startTime + duration - 1;
    
    container.innerHTML = `
        <div class="detail-item">
            <div class="detail-label">课程名称</div>
            <div class="detail-value">${course.name || course.kcmc}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">教师</div>
            <div class="detail-value">${course.teacher || course.jsxm}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">上课地点</div>
            <div class="detail-value">${course.location || 'N/A'}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">上课时间</div>
            <div class="detail-value">
                <div style="margin-bottom: 5px;">
                    ${weekRange ? `<span style="background: #e3f2fd; padding: 2px 6px; border-radius: 4px; font-size: 12px;">第${weekRange}周</span> ` : ''}
                    ${course.dayOfWeek || ''} ${startTime}-${endTime}节
                    ${weekType ? `<span style="margin-left: 5px; padding: 2px 6px; border-radius: 4px; font-size: 12px; background: ${weekType === '单周' ? '#fff3e0' : weekType === '双周' ? '#e8f5e9' : '#f3e5f5'}; color: ${weekType === '单周' ? '#f57c00' : weekType === '双周' ? '#388e3c' : '#7b1fa2'}">${weekType}</span>` : ''}
                </div>
                ${season ? `<div style="color: #666; font-size: 13px;">${season}学期</div>` : ''}
            </div>
        </div>
        <div class="detail-item">
            <div class="detail-label">学分</div>
            <div class="detail-value">${course.credits || ' '}</div>
        </div>
        <div class="detail-item">
            <div class="detail-label">考试时间</div>
            <div class="detail-value">${course.test || ' '}</div>
        </div>
    `;
}
