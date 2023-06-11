-- Insert sample data into `Chairman` table
INSERT INTO `mydb`.`Chairman` (`ChairmanID`, `ChairmanName`, `ChairmanEmail`) VALUES
(1, 'John Doe', 'john.doe@example.com'),
(2, 'Jane Smith', 'jane.smith@example.com');

-- Insert sample data into `Department` table
INSERT INTO `mydb`.`Department` (`DepartmentCode`, `DepartmentName`, `DepartmentEmail`, `College`, `Chairman_ChairmanID`) VALUES
(1, 'Computer Science', 'cs@example.com', 'College of Engineering', 1),
(2, 'Physics', 'physics@example.com', 'College of Science', 2);

-- Insert sample data into `Student` table
INSERT INTO `mydb`.`Student` (`StudentID`, `StudentName`, `StudentEmail`, `StudentMajor`, `Department_DepartmentCode`) VALUES
(1, 'Alice Johnson', 'alice.johnson@example.com', 'Computer Science', 1),
(2, 'Bob Smith', 'bob.smith@example.com', 'Physics', 2);

-- Insert sample data into `Section` table
INSERT INTO `mydb`.`Section` (`SectionNumber`) VALUES
(1),
(2),
(3);

-- Insert sample data into `Student_has_Section` table
INSERT INTO `mydb`.`Student_has_Section` (`Student_StudentID`, `Section_SectionNumber`) VALUES
(1, 1),
(1, 2),
(2, 3);
