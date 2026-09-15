<!DOCTYPE html>
<html lang="zh-TW">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>飄浮氣球互動遊戲</title>
    <style>
        body {
            margin: 0;
            padding: 0;
            height: 100vh;
            background: linear-gradient(to bottom, #87CEEB, #E0F6FF);
            overflow: hidden;
            font-family: Arial, sans-serif;
            user-select: none;
        }

        .hint {
            position: absolute;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: rgba(255, 255, 255, 0.8);
            padding: 10px 20px;
            border-radius: 20px;
            font-size: 16px;
            color: #333;
            pointer-events: none;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
            z-index: 1000;
        }

        .balloon {
            position: absolute;
            width: 50px;
            height: 65px;
            border-radius: 50% 50% 50% 50% / 40% 40% 60% 60%;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            box-shadow: inset -8px -8px 0px rgba(0, 0, 0, 0.15);
        }

        /* 氣球結點 */
        .balloon::after {
            content: "";
            position: absolute;
            bottom: -6px;
            left: 50%;
            transform: translateX(-50%);
            width: 0;
            height: 0;
            border-left: 5px solid transparent;
            border-right: 5px solid transparent;
            border-bottom: 8px solid currentColor;
        }

        /* 氣球線 */
        .balloon::before {
            content: "";
            position: absolute;
            bottom: -22px;
            left: 50%;
            width: 1px;
            height: 16px;
            background-color: rgba(0, 0, 0, 0.3);
        }

        /* 氣球破裂動畫 */
        @keyframes pop {
            0% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.4); opacity: 0.5; }
            100% { transform: scale(0); opacity: 0; }
        }

        .popping {
            animation: pop 0.2s forwards;
        }

        /* 破裂時飛散的小泡泡 */
        .bubble-particle {
            position: absolute;
            width: 8px;
            height: 8px;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.8);
            box-shadow: 0 0 4px rgba(255, 255, 255, 0.9);
            pointer-events: none;
            animation: burst 0.4s ease-out forwards;
        }

        @keyframes burst {
            0% {
                transform: translate(0, 0) scale(1);
                opacity: 1;
            }
            100% {
                transform: translate(var(--dx), var(--dy)) scale(0.2);
                opacity: 0;
            }
        }
    </style>
</head>
<body>

    <div class="hint">左鍵：生成氣球 | 右鍵：戳破氣球</div>

    <script>
        const balloons = [];
        const GRAVITY_Y = 15; // 視窗頂部停留的高度

        // 隨機顏色生成器
        function getRandomColor() {
            const colors = [
                '#FF5733', '#33FF57', '#3357FF', '#F3FF33', 
                '#FF33F3', '#33FFF0', '#FFA533', '#9B33FF'
            ];
            return colors[Math.floor(Math.random() * colors.length)];
        }

        // 左鍵點擊生成氣球
        window.addEventListener('click', (e) => {
            if (e.target.classList.contains('hint')) return;
            createBalloon(e.clientX, e.clientY);
        });

        // 阻止預設右鍵選單，改為戳破氣球
        window.addEventListener('contextmenu', (e) => {
            e.preventDefault();
            const target = e.target.closest('.balloon');
            if (target) {
                popBalloon(target);
            }
        });

        function createBalloon(x, y) {
            const balloon = document.createElement('div');
            balloon.classList.add('balloon');
            
            const color = getRandomColor();
            balloon.style.backgroundColor = color;
            balloon.style.color = color;
            
            const startX = x - 25;
            const startY = y - 32;
            
            balloon.style.left = startX + 'px';
            balloon.style.top = startY + 'px';

            document.body.appendChild(balloon);

            const balloonObj = {
                element: balloon,
                x: startX,
                y: startY,
                vx: (Math.random() - 0.5) * 1.5, // 輕微左右晃動
                vy: -3 - Math.random() * 2,       // 往上飄的速度
                isStopped: false,                 // 是否已到達頂端停住
                bounceTimer: 0,                   // 頂端彈跳動畫計時器
                floatAngle: Math.random() * Math.PI * 2 // 頂端左右漂浮的相位角
            };

            balloons.push(balloonObj);
        }

        function popBalloon(element) {
            const index = balloons.findIndex(b => b.element === element);
            if (index !== -1) {
                const rect = element.getBoundingClientRect();
                createBubbles(rect.left + 25, rect.top + 32);

                element.classList.add('popping');
                setTimeout(() => {
                    element.remove();
                }, 200);
                balloons.splice(index, 1);
            }
        }

        // 生成破裂時的飛散小泡泡
        function createBubbles(x, y) {
            const bubbleCount = 8;
            for (let i = 0; i < bubbleCount; i++) {
                const bubble = document.createElement('div');
                bubble.classList.add('bubble-particle');
                bubble.style.left = x + 'px';
                bubble.style.top = y + 'px';

                const angle = Math.random() * Math.PI * 2;
                const distance = 30 + Math.random() * 40;
                const dx = Math.cos(angle) * distance;
                const dy = Math.sin(angle) * distance;

                bubble.style.setProperty('--dx', dx + 'px');
                bubble.style.setProperty('--dy', dy + 'px');

                document.body.appendChild(bubble);

                setTimeout(() => {
                    bubble.remove();
                }, 400);
            }
        }

        // 動畫循環
        function animate() {
            for (let i = 0; i < balloons.length; i++) {
                let b = balloons[i];

                if (!b.isStopped) {
                    b.x += b.vx;
                    b.y += b.vy;

                    // 左右邊界限制
                    if (b.x < 0) { b.x = 0; b.vx *= -1; }
                    if (b.x > window.innerWidth - 50) { b.x = window.innerWidth - 50; b.vx *= -1; }

                    // 檢查是否碰到視窗頂端
                    if (b.y <= GRAVITY_Y) {
                        b.y = GRAVITY_Y;
                        b.isStopped = true;
                        b.bounceTimer = 0; // 開始頂端彈跳動畫
                    }
                } else {
                    // 已經到達頂端：處理彈跳動畫與左右漂浮
                    b.bounceTimer += 0.15;
                    
                    // 用正弦函數計算水平的左右擺動（漂浮感）
                    b.floatAngle += 0.03;
                    let floatOffsetX = Math.sin(b.floatAngle) * 0.8; // 左右漂浮幅度
                    b.x += floatOffsetX;

                    // 確保漂浮時不超出左右視窗邊界
                    if (b.x < 0) b.x = 0;
                    if (b.x > window.innerWidth - 50) b.x = window.innerWidth - 50;

                    let scaleX = 1;
                    let scaleY = 1;

                    if (b.bounceTimer < Math.PI) {
                        // 剛碰到頂時的擠壓變形
                        scaleY = 1 - Math.sin(b.bounceTimer) * 0.15;
                        scaleX = 1 + Math.sin(b.bounceTimer) * 0.1;
                    }

                    b.element.style.transform = `scale(${scaleX}, ${scaleY})`;
                }

                // 更新 DOM 位置
                b.element.style.left = b.x + 'px';
                b.element.style.top = b.y + 'px';
            }

            requestAnimationFrame(animate);
        }

        // 初始化：網頁載入時自動生成 10 個隨機氣球
        window.addEventListener('load', () => {
            for (let i = 0; i < 10; i++) {
                const randomX = Math.random() * (window.innerWidth - 100) + 50;
                const randomY = Math.random() * (window.innerHeight * 0.4) + (window.innerHeight * 0.5);
                createBalloon(randomX, randomY);
            }
            requestAnimationFrame(animate);
        });
    </script>
</body>
</html>
