## Functional requirement
- Hierarchial Structure
- Commands supported: mkdir, ls, pwd, cd, touch, cat, echo ''>file
- Detailed view of ls -l(metadata like creation time, file or directory)
- Handle both relative and absolute path
## Non-functional requirement
- Handle error messages and exceptions correctly
- Use best patterns to make code clean and extensible

## Entities
- FileSystem(FACADE)
- FileSystemNode(A), File(C), Directory(C)
- Command(I), Command_LS(C), Command_MKDIR(C), Command_PWD(C), Command_CD(C), Command_TOUCH(C), Command_CAT(C), Command_ECHO(C)
- Shell: take input from user and make it a command
- Strategy: ListingStrategy(I), Simple(C), Detailed(C)
