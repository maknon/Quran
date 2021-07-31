<?php

if (empty($_GET['q']))
	$qiraat = 'hafs';
else
	$qiraat = $_GET['q'];

$title = 'القرآن العظيم';

$pageMax = 604;

$baseDirectory = "https://www.maknoon.com/quran/".$qiraat;

if (empty($_GET['p']))
	$currentPage = 1;
else {
	$p = $_GET['p'];
	if(is_numeric($p)) {
		if($p >= 1 && $p <= $pageMax)
			$currentPage = $p;
		else
			$currentPage = 1;
	} else
		$currentPage = 1;
}

echo "<!DOCTYPE html>
<html>
<head>
<meta name='viewport' content='width=device-width, initial-scale=1'>
<title>$title</title>
<link rel='icon' href='favicon.png'>
<style>
body {
	margin: 0;
	background-color: #FFFFF2;
}

.navbar {
	overflow: hidden;
	background-color: transparent;
	position: fixed;
	top: 0px;
	left: 0px;
	border: 1px solid #ccc;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.navbar a {
	float: left;
	display: block;
	padding: 10px 10px;
}

.navbar a:hover {
	background: rgba(221, 221, 221, 0.3);
}

.img {
	width:100%;
	margin: auto;
}

.img-container {
	position: absolute;
	top: 0;
	bottom: 0;
	left: 0;
	right: 0;
	display: flex;
}

.disabled {
	pointer-events: none;
	opacity: 0.2;
}

.dropdown-content-sura {
	display: none;
	position: fixed;
	background-color: #f1f1f1;
	min-width: 130px;
	z-index: 1;
	left: 45px;
	text-align:right;
	max-height:400px; /* you can change as you need it */
	overflow:auto; /* to get scroll */
}

.dropdown-content-sura a {
	color: black;
	padding: 12px 16px;
	display: block;
}

.dropdown-content-juz {
	display: none;
	position: fixed;
	background-color: #f1f1f1;
	min-width: 130px;
	z-index: 1;
	left: 45px;
	text-align:right;
	max-height:400px; /* you can change as you need it */
	overflow:auto; /* to get scroll */
}

.dropdown-content-juz a {
	color: black;
	padding: 12px 16px;
	display: block;
}

.show {
	display: flex;
	flex-direction: column;
}

</style>
</head>
<body>

<div class='img-container'>
	<img src='$baseDirectory/$currentPage.svgz' id='page' class='img'>
</div>

<div class='navbar'>
	<div>
		<a onclick='swap()'>
			<img src='swap.svg' id='swap' alt='Collapse'>
		</a>
	</div>
	<div id='sura'>
		<a onclick='showSura()' class='suraBtn'>
			<img src='sura.svg' alt='Show sura list' class='suraBtn'>
		</a>
		<div id='suraList' class='dropdown-content-sura'>
			<a onclick='suraGoTo(1)'>الفاتحة</a>
			<a onclick='suraGoTo(2)'>البقرة</a>
			<a onclick='suraGoTo(50)'>آل عمران</a>
			<a onclick='suraGoTo(77)'>النساء</a>
			<a onclick='suraGoTo(106)'>المائدة</a>
			<a onclick='suraGoTo(128)'>الأنعام</a>
			<a onclick='suraGoTo(151)'>الأعراف</a>
			<a onclick='suraGoTo(177)'>الأنفال</a>
			<a onclick='suraGoTo(187)'>التوبة</a>
			<a onclick='suraGoTo(208)'>يونس</a>
			<a onclick='suraGoTo(221)'>هود</a>
			<a onclick='suraGoTo(235)'>يوسف</a>
			<a onclick='suraGoTo(249)'>الرعد</a>
			<a onclick='suraGoTo(255)'>إبراهيم</a>
			<a onclick='suraGoTo(262)'>الحجر</a>
			<a onclick='suraGoTo(267)'>النحل</a>
			<a onclick='suraGoTo(282)'>الإسراء</a>
			<a onclick='suraGoTo(293)'>الكهف</a>
			<a onclick='suraGoTo(305)'>مريم</a>
			<a onclick='suraGoTo(312)'>طه</a>
			<a onclick='suraGoTo(322)'>الأنبياء</a>
			<a onclick='suraGoTo(332)'>الحج</a>
			<a onclick='suraGoTo(342)'>المؤمنون</a>
			<a onclick='suraGoTo(350)'>النور</a>
			<a onclick='suraGoTo(359)'>الفرقان</a>
			<a onclick='suraGoTo(367)'>الشعراء</a>
			<a onclick='suraGoTo(377)'>النمل</a>
			<a onclick='suraGoTo(385)'>القصص</a>
			<a onclick='suraGoTo(396)'>العنكبوت</a>
			<a onclick='suraGoTo(404)'>الروم</a>
			<a onclick='suraGoTo(411)'>لقمان</a>
			<a onclick='suraGoTo(415)'>السجدة</a>
			<a onclick='suraGoTo(418)'>الأحزاب</a>
			<a onclick='suraGoTo(428)'>سبأ</a>
			<a onclick='suraGoTo(434)'>فاطر</a>
			<a onclick='suraGoTo(440)'>يس</a>
			<a onclick='suraGoTo(446)'>الصافات</a>
			<a onclick='suraGoTo(453)'>ص</a>
			<a onclick='suraGoTo(458)'>الزمر</a>
			<a onclick='suraGoTo(467)'>غافر</a>
			<a onclick='suraGoTo(477)'>فصلت</a>
			<a onclick='suraGoTo(483)'>الشورى</a>
			<a onclick='suraGoTo(489)'>الزخرف</a>
			<a onclick='suraGoTo(496)'>الدخان</a>
			<a onclick='suraGoTo(499)'>الجاثية</a>
			<a onclick='suraGoTo(502)'>الأحقاف</a>
			<a onclick='suraGoTo(507)'>محمد</a>
			<a onclick='suraGoTo(511)'>الفتح</a>
			<a onclick='suraGoTo(515)'>الحجرات</a>
			<a onclick='suraGoTo(518)'>ق</a>
			<a onclick='suraGoTo(520)'>الذاريات</a>
			<a onclick='suraGoTo(523)'>الطور</a>
			<a onclick='suraGoTo(526)'>النجم</a>
			<a onclick='suraGoTo(528)'>القمر</a>
			<a onclick='suraGoTo(531)'>الرحمن</a>
			<a onclick='suraGoTo(534)'>الواقعة</a>
			<a onclick='suraGoTo(537)'>الحديد</a>
			<a onclick='suraGoTo(542)'>المجادلة</a>
			<a onclick='suraGoTo(545)'>الحشر</a>
			<a onclick='suraGoTo(549)'>الممتحنة</a>
			<a onclick='suraGoTo(551)'>الصف</a>
			<a onclick='suraGoTo(553)'>الجمعة</a>
			<a onclick='suraGoTo(554)'>المنافقون</a>
			<a onclick='suraGoTo(556)'>التغابن</a>
			<a onclick='suraGoTo(558)'>الطلاق</a>
			<a onclick='suraGoTo(560)'>التحريم</a>
			<a onclick='suraGoTo(562)'>الملك</a>
			<a onclick='suraGoTo(564)'>القلم</a>
			<a onclick='suraGoTo(566)'>الحاقة</a>
			<a onclick='suraGoTo(568)'>المعارج</a>
			<a onclick='suraGoTo(570)'>نوح</a>
			<a onclick='suraGoTo(572)'>الجن</a>
			<a onclick='suraGoTo(574)'>المزمل</a>
			<a onclick='suraGoTo(575)'>المدثر</a>
			<a onclick='suraGoTo(577)'>القيامة</a>
			<a onclick='suraGoTo(578)'>الإنسان</a>
			<a onclick='suraGoTo(580)'>المرسلات</a>
			<a onclick='suraGoTo(582)'>النبأ</a>
			<a onclick='suraGoTo(583)'>النازعات</a>
			<a onclick='suraGoTo(585)'>عبس</a>
			<a onclick='suraGoTo(586)'>التكوير</a>
			<a onclick='suraGoTo(587)'>الإنفطار</a>
			<a onclick='suraGoTo(587)'>المطففين</a>
			<a onclick='suraGoTo(589)'>الانشقاق</a>
			<a onclick='suraGoTo(590)'>البروج</a>
			<a onclick='suraGoTo(591)'>الطارق</a>
			<a onclick='suraGoTo(591)'>الأعلى</a>
			<a onclick='suraGoTo(592)'>الغاشية</a>
			<a onclick='suraGoTo(593)'>الفجر</a>
			<a onclick='suraGoTo(594)'>البلد</a>
			<a onclick='suraGoTo(595)'>الشمس</a>
			<a onclick='suraGoTo(595)'>الليل</a>
			<a onclick='suraGoTo(596)'>الضحى</a>
			<a onclick='suraGoTo(596)'>الشرح</a>
			<a onclick='suraGoTo(597)'>التين</a>
			<a onclick='suraGoTo(597)'>العلق</a>
			<a onclick='suraGoTo(598)'>القدر</a>
			<a onclick='suraGoTo(598)'>البينة</a>
			<a onclick='suraGoTo(599)'>الزلزلة</a>
			<a onclick='suraGoTo(599)'>العاديات</a>
			<a onclick='suraGoTo(600)'>القارعة</a>
			<a onclick='suraGoTo(600)'>التكاثر</a>
			<a onclick='suraGoTo(601)'>العصر</a>
			<a onclick='suraGoTo(601)'>الهمزة</a>
			<a onclick='suraGoTo(601)'>الفيل</a>
			<a onclick='suraGoTo(602)'>قريش</a>
			<a onclick='suraGoTo(602)'>الماعون</a>
			<a onclick='suraGoTo(602)'>الكوثر</a>
			<a onclick='suraGoTo(603)'>الكافرون</a>
			<a onclick='suraGoTo(603)'>النصر</a>
			<a onclick='suraGoTo(603)'>المسد</a>
			<a onclick='suraGoTo(604)'>الإخلاص</a>
			<a onclick='suraGoTo(604)'>الفلق</a>
			<a onclick='suraGoTo(604)'>الناس</a>
		</div>
	</div>
	<div id='juz'>
		<a onclick='showJuz()' class='juzBtn'>
			<img src='juz.svg' alt='Show juz list' class='juzBtn'>
		</a>
		<div id='juzList' class='dropdown-content-juz'>
			<a onclick='suraGoTo(1)'>الجزء 1</a>
			<a onclick='suraGoTo(22)'>الجزء 2</a>
			<a onclick='suraGoTo(42)'>الجزء 3</a>
			<a onclick='suraGoTo(62)'>الجزء 4</a>
			<a onclick='suraGoTo(82)'>الجزء 5</a>
			<a onclick='suraGoTo(102)'>الجزء 6</a>
			<a onclick='suraGoTo(122)'>الجزء 7</a>
			<a onclick='suraGoTo(142)'>الجزء 8</a>
			<a onclick='suraGoTo(162)'>الجزء 9</a>
			<a onclick='suraGoTo(182)'>الجزء 10</a>
			<a onclick='suraGoTo(202)'>الجزء 11</a>
			<a onclick='suraGoTo(222)'>الجزء 12</a>
			<a onclick='suraGoTo(242)'>الجزء 13</a>
			<a onclick='suraGoTo(262)'>الجزء 14</a>
			<a onclick='suraGoTo(282)'>الجزء 15</a>
			<a onclick='suraGoTo(302)'>الجزء 16</a>
			<a onclick='suraGoTo(322)'>الجزء 17</a>
			<a onclick='suraGoTo(342)'>الجزء 18</a>
			<a onclick='suraGoTo(362)'>الجزء 19</a>
			<a onclick='suraGoTo(382)'>الجزء 20</a>
			<a onclick='suraGoTo(402)'>الجزء 21</a>
			<a onclick='suraGoTo(422)'>الجزء 22</a>
			<a onclick='suraGoTo(442)'>الجزء 23</a>
			<a onclick='suraGoTo(462)'>الجزء 24</a>
			<a onclick='suraGoTo(482)'>الجزء 25</a>
			<a onclick='suraGoTo(502)'>الجزء 26</a>
			<a onclick='suraGoTo(522)'>الجزء 27</a>
			<a onclick='suraGoTo(542)'>الجزء 28</a>
			<a onclick='suraGoTo(562)'>الجزء 29</a>
			<a onclick='suraGoTo(582)'>الجزء 30</a>
		</div>
	</div>
	<div id='last'>
		<a onclick='lastPage()'>
			<img src='last.svg' alt='Last Page'>
		</a>
	</div>
	<div id='next'>
		<a onclick='nextPage()'>
			<img src='forward.svg' alt='Next Page'>
		</a>
	</div>
	<div id='pageInput'>
		<input value='$currentPage' id='inp' type='text' autocomplete='off' onkeydown='goTo(this)' style='padding:5px;font-size:15px;font-family:Arial;text-align:center;outline:none;background:transparent;border:1px solid;border-color:rgb(200,200,200,0.5);width:2rem;'>
	</div>
	<div id='maxP'>
		<label style='background:rgb(200,200,200,0.5);padding:5px;font-size:15px;font-family:Arial;display:inline-block;text-align:center;border:1px solid;border-color:rgb(200,200,200,0.5);width:2rem;'>$pageMax</label>
	</div>
	<div id='back'>
		<a onclick='previousPage()'>
			<img src='back.svg' alt='Previous Page'>
		</a>
	</div>
	<div id='first'>
		<a onclick='firstPage()'>
			<img src='first.svg' alt='First Page'>
		</a>
	</div>
	<div id='pdf'>
		<a href='https://www.maknoon.com/download/quran/quran_${qiraat}_m.pdf'>
			<img src='pdf.svg' alt='Download as PDF'>
		</a>
	</div>
	<div id='home'>
		<a href='https://www.maknoon.com/community/forums/16'>
			<img src='home.svg' alt='Back to Main'>
		</a>
	</div>
</div>
";

echo "
<script type='text/javascript'>
	var currentPage = $currentPage;
	var pageMax = $pageMax;

	function firstPage() {
		currentPage = 1;
		document.getElementById('page').src = `$baseDirectory/1.svgz`;
		document.getElementById('inp').value = `\${currentPage}`;
		window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
		disable();
	}
	
	function lastPage() {
		currentPage = pageMax;
		document.getElementById('page').src = `$baseDirectory/\${currentPage}.svgz`;
		document.getElementById('inp').value = `\${currentPage}`;
		window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
		disable();
	}
	
	function nextPage() {
		if((currentPage + 1) <= pageMax) {
			currentPage = currentPage + 1;
			document.getElementById('page').src = `$baseDirectory/\${currentPage}.svgz`;
			document.getElementById('inp').value = `\${currentPage}`;
			window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
			disable();
		}
	}
	
	function previousPage() {
		if((currentPage - 1) > 0) {
			currentPage = currentPage - 1;
			document.getElementById('page').src = `$baseDirectory/\${currentPage}.svgz`;
			document.getElementById('inp').value = `\${currentPage}`;
			window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
			disable();
		}
	}
	
	function disable() {
		document.getElementById('next').className = 'n';
		document.getElementById('last').className = 'n';
		document.getElementById('first').className = 'n';
		document.getElementById('back').className = 'n';
		
		if ((currentPage + 1) > pageMax) {
			document.getElementById('next').className = 'disabled';
			document.getElementById('last').className = 'disabled';
		}

		if ((currentPage - 1) == 0) {
			document.getElementById('first').className = 'disabled';
			document.getElementById('back').className = 'disabled';
		}
	}
	
	document.addEventListener('DOMContentLoaded', function() {
		disable();
	}, false);
	
	// When the user clicks on the button, toggle between hiding and showing the dropdown content
	function showSura() {
		document.getElementById('suraList').classList.toggle('show');
	}
	
	function showJuz() {
		document.getElementById('juzList').classList.toggle('show');
	}
	
	// Close the dropdown if the user clicks outside of it
	window.onclick = function(event) {
		if (!event.target.matches('.suraBtn')) {
			var dropdowns = document.getElementsByClassName('dropdown-content-sura');
			var i;
			for (i = 0; i < dropdowns.length; i++) {
				var openDropdown = dropdowns[i];
				if (openDropdown.classList.contains('show')) {
					openDropdown.classList.remove('show');
				}
			}
		}
		
		if (!event.target.matches('.juzBtn')) {
			var dropdowns = document.getElementsByClassName('dropdown-content-juz');
			var i;
			for (i = 0; i < dropdowns.length; i++) {
				var openDropdown = dropdowns[i];
				if (openDropdown.classList.contains('show')) {
					openDropdown.classList.remove('show');
				}
			}
		}
	}

	function goTo(e) {
		if(event.keyCode == 13) {
			var page = Number(e.value);
			if(isNaN(page)) {
				document.getElementById('inp').value = `\${currentPage}`;
			} else {
				if(page >= 1 && page <= pageMax) {
					currentPage = page;
					document.getElementById('page').src = `$baseDirectory/\${currentPage}.svgz`;
					window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
				} else {
					document.getElementById('inp').value = `\${currentPage}`;
				}
			}
			disable();
		}
	}
	
	function suraGoTo(e) {
		currentPage = e;
		document.getElementById('page').src = `$baseDirectory/\${currentPage}.svgz`;
		document.getElementById('inp').value = `\${currentPage}`;
		window.history.pushState({}, '$title', `/quran/view.php?q=$qiraat&p=\${currentPage}`);
		disable();
	}
	
	function swap() {
		if(document.getElementById('swap').alt == 'Collapse') {
			document.getElementById('home').style.display = 'none';
			document.getElementById('first').style.display = 'none';
			document.getElementById('next').style.display = 'none';
			document.getElementById('pageInput').style.display = 'none';
			document.getElementById('maxP').style.display = 'none';
			document.getElementById('last').style.display = 'none';
			document.getElementById('back').style.display = 'none';
			document.getElementById('pdf').style.display = 'none';
			document.getElementById('sura').style.display = 'none';
			document.getElementById('juz').style.display = 'none';
			document.getElementById('swap').alt = 'Expand';
		} else {
			document.getElementById('home').style.display = 'block';
			document.getElementById('first').style.display = 'block';
			document.getElementById('next').style.display = 'block';
			document.getElementById('pageInput').style.display = 'block';
			document.getElementById('maxP').style.display = 'block';
			document.getElementById('last').style.display = 'block';
			document.getElementById('back').style.display = 'block';
			document.getElementById('pdf').style.display = 'block';
			document.getElementById('sura').style.display = 'block';
			document.getElementById('juz').style.display = 'block';
			document.getElementById('swap').alt = 'Collapse';
		}
	}

</script>

</body>
</html>
";
?>
