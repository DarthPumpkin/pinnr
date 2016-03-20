# Pinnr
## Description
Pinnr is an Android app that helps you remember your PIN by mapping it to an actual word using the
same mapping as you have on mobile phones (a, b, or c maps to `2`, d, e or f maps to `3`, etc.).

## Developer instructions
This is a gradle project, you can simply do `./gradlew build` and everything should work just fine.
The build involves creating the sqlite database the app is shipped with. It is created from a
built-in word list. This text file may be replaced with any other word list of your choice by
setting `fileIn` in the build script to the path of your text file.

You can specifically create/delete the database with the gradle tasks `sqliteFromText` and
`cleanSqliteFromText`, respectively.
