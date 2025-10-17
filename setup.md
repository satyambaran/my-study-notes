Create a repository on github

```
git init

# Add all files
git add .

# Commit your changes
git commit -m "Initial commit"

# Add remote repository
git remote add origin https://github.com/username/repository-name.git

# Rename branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main

# Set your email to GitHub's no-reply email
git config user.email "satyambaran@users.noreply.github.com"

# Amend your last commit with the new email
git commit --amend --reset-author --no-edit

# Now push
git push -u origin main
```