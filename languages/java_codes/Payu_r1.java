public class Payu_r1 {

}

/*
 * Given an array of integers nums containing n + 1 integers where each integer
 * is in the range [1, n] inclusive.
 * There is only one repeated number in nums, return this repeated number.
 * You must solve the problem without modifying the array nums and uses only
 * constant extra space.
 * 
 * 
 * Example 1:
 * Input: nums = [1,3,4,2,2]Output: 2
 * Example 2:
 * Input: nums = [3,1,3,4,2]Output: 3
 * Example 3:
 * Input: nums = [3,3,3,3,3]Output: 3
 * 
 * 1 2 4 3 3 3 = 16 = 21
 * 1 2 3 5 1 1 = 13 = 21
 * 1 2 4 5 5 5 = 22 = 21
 * 
 * for(i=0-n) for(j=i+1-n)
 * 
 * 1-n freq = 0 or 1 except one element
 * 
 * l=1 mid r=n := nlogn take decisions based on number of higher and lower element
 */
/* 
int add(int a, int b, int c) {
}

int add(int a, int b) {
}

class add
subclass1 overriden add
subclass2 overriden add

class[]:=scObj
scObj.add()

covariant data type: Covariance in programming refers to the ability of a subtype to replace a supertype in contexts where the supertype is expected.
*/
/* 
Student
Id	Student name
1	AAAA
2	BBBB

Subject
Id	Subject name
1	Subject1
2	Subject2

Marks
student_id	subject_id	marks
1	1	33
1	2	34
2	1	54
2	2	42

Requirement is like, list all students with Pass (>=35 marks in all subject) or Fail (<35 marks in any subject) status
 
Expected output
 
 
student_id	student_name	status
1	AAAA	Fail
2	BBBB	Pass


select student_id, student_name, "Fail" as status from Student where id in (select student_id from Marks where marks<35) 
union
select id, name, "Pass" as status from Student where id not in (select student_id from Marks where marks<35)

select 
    s.id as student_id, 
    s.name as student_name, 
    case 
        when min(m.marks) < 35 then 'Fail'
        else 'Pass'
    end as status
from Student s
left join Marks m on s.id = m.student_id
group by s.id, s.name;
*/