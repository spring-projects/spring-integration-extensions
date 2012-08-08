declare variable $name as xs:string external;
declare variable $class as xs:int external;
for 	$student in /mappings/students/student,
		$subject in /mappings/subjects/subject
	where	$student/@id = $subject/students/studentId
	and	$student/name = $name
	and	$student/class = $class
return $subject/name/text()