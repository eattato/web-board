-- --------------------------------------------------------
-- 호스트:                          127.0.0.1
-- 서버 버전:                        10.9.2-MariaDB - mariadb.org binary distribution
-- 서버 OS:                        Win64
-- HeidiSQL 버전:                  12.1.0.6537
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

CREATE TABLE board.members(
	email VARCHAR(320) NOT NULL,
	pw CHAR(64) NOT NULL,
	nickname VARCHAR(20) NOT NULL,
	verify INT NOT NULL,
	faceimg VARCHAR(100) NULL,
	about VARCHAR(60) NULL,
	isadmin BOOLEAN NULL,
	vcode VARCHAR(6) NULL,
	PRIMARY KEY (email)
) DEFAULT CHARSET=utf8;

CREATE TABLE categories(
	id INT NOT NULL,
	category VARCHAR(100) NOT NULL,
	about VARCHAR(300) NULL,
	img VARCHAR(100) NULL,
	anonymous BOOLEAN NULL,
	adminonly BOOLEAN NULL,
	admins TEXT NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET=utf8;

CREATE TABLE board.posts(
	id INT NOT NULL,
	category INT NOT NULL,
	postname VARCHAR(100) NOT NULL,
	author VARCHAR(320) NOT NULL,
	postdate DATETIME NOT NULL,
	content TEXT NOT NULL,
	loved INT NULL,
	hated INT NULL,
	viewers INT NULL,
	taglist VARCHAR(300) NULL,
	lovers TEXT NULL,
	haters TEXT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (category) REFERENCES board.categories (id),
	FOREIGN KEY (author) REFERENCES board.members (email)
) DEFAULT CHARSET=UTF8;

CREATE TABLE comments(
	id INT NOT NULL,
	post INT NOT NULL,
	author VARCHAR(320) NOT NULL,
	reply INT NOT NULL,
	content VARCHAR(200) NOT NULL,
	postdate DATE NOT NULL,
	PRIMARY KEY (id),
	FOREIGN KEY (post) REFERENCES posts (id),
	FOREIGN KEY (author) REFERENCES members (email)
) DEFAULT CHARSET=UTF8;

CREATE TABLE auditlog(
	id INT NOT NULL,
	logdate DATETIME NOT NULL,
	content TEXT NOT NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET=UTF8;

CREATE TABLE tags(
	id INT NOT NULL,
	tagname VARCHAR(30) not null,
	tagdesc VARCHAR(300) NULL,
	adminonly BOOLEAN NULL,
	tagcolor CHAR(6) NULL,
	PRIMARY KEY (id)
) DEFAULT CHARSET=UTF8;

-- 테이블 데이터 board.auditlog:~0 rows (대략적) 내보내기

-- 테이블 데이터 board.categories:~1 rows (대략적) 내보내기
INSERT INTO `categories` (`id`, `category`, `about`, `img`, `anonymous`, `adminonly`, `admins`) VALUES
	(1, '자유게시판', '게시물 종류 상관없이 자유롭게 게시할 수 있는 게시판입니다.', NULL, NULL, NULL, 'eattato0804@gmail.com');
SELECT * FROM categories;

-- 테이블 데이터 board.comments:~3 rows (대략적) 내보내기
INSERT INTO `comments` (`id`, `post`, `author`, `reply`, `content`, `postdate`) VALUES
	(1, 1, 'eattato0804@naver.com', -1, '엄준식은 살아있다!!', '2022-09-29'),
	(4, 1, 'eattato0804@naver.com', 1, '아닌듯', '2022-09-29'),
	(6, 1, 'abcd@efg', -1, '고추참치 고추참치 참치 참치 고추참치\n냉장고를 열어봐라 고추참치 꺼내 먹어라', '2022-10-06');
SELECT * FROM comments;

-- 테이블 데이터 board.members:~8 rows (대략적) 내보내기
INSERT INTO `members` (`email`, `pw`, `nickname`, `verify`, `faceimg`, `about`, `isadmin`, `vcode`) VALUES
	('abcd@efg', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', 'asdfasdf', 0, 'profiles/60e94465-29fd-4fa9-a85c-df2af20c1b29.png', NULL, 0, NULL),
	('asdf@asdf', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', '엄준식은살아있냐?모른다 ㅋ', 0, NULL, NULL, 0, NULL),
	('eattato0804@gmail.com', '595c272486cd56b72af6093cf2a68ead6874bfdde2b896de588f3f8404dbceb5', 'eattato', 1, NULL, '안녕? 이것 계정 는 그 공식인 계정 의 그 개발자 의 그 보드 방주 웹사이트. 아마도', 1, NULL),
	('eattato0804@naver.com', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', 'BOARDARC', 0, 'profiles/b81108d7-3383-455a-8193-5086dece8503.png', NULL, 0, '7udd70'),
	('ejrwns1194@gmail.com', '1576c39850ea527db2e1b35004d061f2928bb50a0a0fbfa1d9a36c8523bb100b', 'Bdof', 0, NULL, NULL, 0, NULL),
	('umjunsik@mollu.kr', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', 'umjunsik', 0, NULL, NULL, 0, NULL),
	('umjunsik@mollu.kro', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', 'umjunsik', 0, NULL, NULL, 0, NULL),
	('umjunsik@mollu.net', 'cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268', 'umjunsik', 0, NULL, NULL, 0, NULL);
SELECT * FROM members;

-- 테이블 데이터 board.posts:~13 rows (대략적) 내보내기
INSERT INTO `posts` (`id`, `category`, `postname`, `author`, `postdate`, `content`, `loved`, `hated`, `viewers`, `taglist`, `lovers`, `haters`) VALUES
	(1, 1, 'ㅁ', 'eattato0804@naver.com', '2022-09-28 00:00:00', '<p>ㅁ</p>', 0, 0, 116, '1', NULL, NULL),
	(2, 1, '테스트', 'eattato0804@naver.com', '2022-09-28 00:00:00', '<p><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">맺어, 것은 듣기만 실현에 가지에 만천하의 그들의 때문이다. 할지라도 새가 하였으며, 우리는 들어 가는 피가 어디 착목한는 황금시대다. 충분히 유소년에게서 거친 용감하고 것은 그들은 철환하였는가? 착목한는 우리 있음으로써 무엇을 봄바람이다. 이것은 밝은 얼음이 힘있다. 풀이 커다란 얼음과 힘차게 설레는 사람은 작고 있으랴? 꽃 있을 할지니, 대중을 목숨이 피어나는 품고 것이다. 피어나기 가슴에 노래하며 이것이야말로 있음으로써 없으면, 길을 가치를 있다. 청춘의 너의 긴지라 꽃 같이, 설산에서 것은 봄바람이다. 바이며, 가는 그들의 피에 있는가?</span><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">산야에 날카로우나 간에 타오르고 주며, 그들은 피고, 하였으며, 거친 보라. 반짝이는 천고에 천하를 봄바람을 위하여 있으며, 싸인 황금시대다. 넣는 끝에 맺어, 천하를 것이다. 구하지 광야에서 오직 예가 없으면, 구하기 인생의 있으랴? 우리 뭇 위하여, 사막이다. 놀이 그들에게 이상의 간에 얼음에 이상을 이상은 봄바람이다. 풀밭에 동산에는 우리 이는 오직 두손을 너의 기쁘며, 청춘의 교향악이다. 이상이 역사를 풀밭에 피가 열락의 힘있다. 관현악이며, 우는 열매를 기쁘며, 것이다. 가슴이 사랑의 위하여 앞이 이것을 하였으며, 것이다.</span><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">영락과 과실이 그들을 무엇을 스며들어 사라지지 있는가? 웅대한 찾아 이상의 얼음과 스며들어 영원히 것이 말이다. 듣기만 크고 얼음과 사는가 때문이다. 얼음에 그들의 이상 것이다. 수 석가는 되는 가진 얼마나 힘차게 청춘의 청춘의 황금시대다. 그와 노래하며 있으며, 것은 이상은 이성은 불어 청춘 곧 봄바람이다. 가치를 이상 그림자는 교향악이다. 끓는 예수는 속잎나고, 청춘의 사람은 청춘 그들은 할지라도 아니한 운다. 얼마나 대한 트고, 꽃이 원질이 보라.</span>&nbsp;</p>', 0, 0, 6, NULL, NULL, NULL),
	(3, 1, '테스트2', 'eattato0804@naver.com', '2022-09-28 00:00:00', '<p><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">맺어, 것은 듣기만 실현에 가지에 만천하의 그들의 때문이다. 할지라도 새가 하였으며, 우리는 들어 가는 피가 어디 착목한는 황금시대다. 충분히 유소년에게서 거친 용감하고 것은 그들은 철환하였는가? 착목한는 우리 있음으로써 무엇을 봄바람이다. 이것은 밝은 얼음이 힘있다. 풀이 커다란 얼음과 힘차게 설레는 사람은 작고 있으랴? 꽃 있을 할지니, 대중을 목숨이 피어나는 품고 것이다. 피어나기 가슴에 노래하며 이것이야말로 있음으로써 없으면, 길을 가치를 있다. 청춘의 너의 긴지라 꽃 같이, 설산에서 것은 봄바람이다. 바이며, 가는 그들의 피에 있는가?</span><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">산야에 날카로우나 간에 타오르고 주며, 그들은 피고, 하였으며, 거친 보라. 반짝이는 천고에 천하를 봄바람을 위하여 있으며, 싸인 황금시대다. 넣는 끝에 맺어, 천하를 것이다. 구하지 광야에서 오직 예가 없으면, 구하기 인생의 있으랴? 우리 뭇 위하여, 사막이다. 놀이 그들에게 이상의 간에 얼음에 이상을 이상은 봄바람이다. 풀밭에 동산에는 우리 이는 오직 두손을 너의 기쁘며, 청춘의 교향악이다. 이상이 역사를 풀밭에 피가 열락의 힘있다. 관현악이며, 우는 열매를 기쁘며, 것이다. 가슴이 사랑의 위하여 앞이 이것을 하였으며, 것이다.</span><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><br style="box-sizing: inherit; color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px;"><span style="color: rgb(96, 108, 118); font-family: -apple-system, system-ui, BlinkMacSystemFont, &quot;apple gothic&quot;, 돋움, Dotum, &quot;helvetica neue&quot;, Helvetica, Arial, sans-serif; font-size: 16px; letter-spacing: 0.16px; background-color: rgb(244, 245, 246);">영락과 과실이 그들을 무엇을 스며들어 사라지지 있는가? 웅대한 찾아 이상의 얼음과 스며들어 영원히 것이 말이다. 듣기만 크고 얼음과 사는가 때문이다. 얼음에 그들의 이상 것이다. 수 석가는 되는 가진 얼마나 힘차게 청춘의 청춘의 황금시대다. 그와 노래하며 있으며, 것은 이상은 이성은 불어 청춘 곧 봄바람이다. 가치를 이상 그림자는 교향악이다. 끓는 예수는 속잎나고, 청춘의 사람은 청춘 그들은 할지라도 아니한 운다. 얼마나 대한 트고, 꽃이 원질이 보라.</span>&nbsp;</p>', 0, 0, 2, NULL, NULL, NULL),
	(4, 1, '코드', 'eattato0804@naver.com', '2022-09-28 00:00:00', '<div style="color: rgb(212, 212, 212); background-color: rgb(30, 30, 30); font-family: Consolas, &quot;Courier New&quot;, monospace; font-size: 14px; line-height: 19px; white-space: pre;"><span style="color: #ce9178;">"<a href="http://localhost:8888/posts/">http://localhost:8888/posts/</a>"</span> + <span style="color: #9cdcfe;">result</span></div>', 0, 0, 0, NULL, NULL, NULL),
	(7, 1, '시큐레이어', 'abcd@efg', '2022-09-28 00:00:00', '<p style="text-align: center; " align="center">시큐레이어</p><p style="text-align: left;" align="left">왼쪽</p>', 0, 0, 6, NULL, NULL, NULL),
	(8, 1, 'DTO 잘됨?', 'eattato0804@naver.com', '2022-09-29 00:00:00', 'ㅇㅇ 잘됨', 0, 0, 4, NULL, NULL, NULL),
	(9, 1, '공지 테스트', 'eattato0804@naver.com', '2022-10-06 00:00:00', '<p>공지 테스트입니다</p>', 0, 0, 5, '1', NULL, NULL),
	(10, 1, '엄준식한 태그를 가진 엄준식한 글이 수정된 글', 'eattato0804@naver.com', '2022-10-06 00:00:00', '<p>엄준식은 살아있다!!</p>', 0, 0, 45, '1 2', NULL, NULL),
	(11, 1, '한동영이 나한테 지건 날린썰 풉니다', 'eattato0804@naver.com', '2022-10-11 00:00:00', '<p>겁나 아파요</p>', 0, 0, 2, '1', NULL, NULL),
	(12, 1, '(충격) 제목이 무려 30 글자인 글이 있다?!?!?!', 'eattato0804@naver.com', '2022-10-19 00:00:00', '<p>와!</p>', 0, 0, 6, '1 2', NULL, NULL),
	(13, 1, '글 작성 외 안됨???', 'eattato0804@naver.com', '2022-10-20 00:00:00', '<p>ㅁ</p>', 1, 2, 92, '', ' eattato0804@naver.com', ' eattato0804@naver.com eattato0804@gmail.com'),
	(14, 1, 'asdfasdf', 'eattato0804@naver.com', '2022-10-20 00:00:00', '<p>asdf</p>', 0, 0, 3, '', NULL, NULL),
	(15, 1, '점검', 'eattato0804@gmail.com', '2022-10-23 00:00:00', '<p>ㅁ</p>', 1, 0, 21, '2', ' eattato0804@gmail.com', '');
SELECT * FROM posts;

-- 테이블 데이터 board.tags:~2 rows (대략적) 내보내기
INSERT INTO `tags` (`id`, `tagname`, `tagdesc`, `adminonly`, `tagcolor`) VALUES
	(1, '공지', '공지사항 게시물에 붙일 수 있는 태그입니다.', 1, 'FF0000'),
	(2, '엄준식', '이게 태그 이름이냐? ㅋㅋ', 0, 'FFFF00');
SELECT * FROM tags;

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
