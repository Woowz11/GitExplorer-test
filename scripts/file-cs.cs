using System;
using System.Collections.Generic;

namespace StudentManagement
{
    // Класс, представляющий студента
    public class Student
    {
        public string Name { get; set; }
        public int Age { get; set; }
        public string Major { get; set; }

        public Student(string name, int age, string major)
        {
            Name = name;
            Age = age;
            Major = major;
        }

        public override string ToString()
        {
            return $"Name: {Name}, Age: {Age}, Major: {Major}";
        }
    }

    // Класс для управления студентами
    public class StudentManager
    {
        private List<Student> students;

        public StudentManager()
        {
            students = new List<Student>();
        }

        // Метод для добавления студента
        public void AddStudent(Student student)
        {
            students.Add(student);
            Console.WriteLine($"Student {student.Name} added.");
        }

        // Метод для удаления студента по имени
        public bool RemoveStudent(string name)
        {
            var student = students.Find(s => s.Name.Equals(name, StringComparison.OrdinalIgnoreCase));
            if (student != null)
            {
                students.Remove(student);
                Console.WriteLine($"Student {name} removed.");
                return true;
            }
            Console.WriteLine($"Student {name} not found.");
            return false;
        }

        // Метод для отображения всех студентов
        public void DisplayStudents()
        {
            if (students.Count == 0)
            {
                Console.WriteLine("No students found.");
                return;
            }

            Console.WriteLine("List of students:");
            foreach (var student in students)
            {
                Console.WriteLine(student);
            }
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            StudentManager manager = new StudentManager();

            // Добавляем несколько студентов
            manager.AddStudent(new Student("Alice", 20, "Computer Science"));
            manager.AddStudent(new Student("Bob", 22, "Mathematics"));
            manager.AddStudent(new Student("Charlie", 21, "Physics"));

            // Отображаем всех студентов
            manager.DisplayStudents();

            // Удаляем студента
            manager.RemoveStudent("Bob");

            // Отображаем всех студентов после удаления
            manager.DisplayStudents();

            // Обработка исключений
            try
            {
                // Попытка удалить несуществующего студента
                manager.RemoveStudent("David");
            }
            catch (Exception ex)
            {
                Console.WriteLine($"An error occurred: {ex.Message}");
            }

            // Добавляем еще одного студента
            manager.AddStudent(new Student("Eve", 23, "Biology"));

            // Отображаем всех студентов после добавления
            manager.DisplayStudents();
        }
    }
}
