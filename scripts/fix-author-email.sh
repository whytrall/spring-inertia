#!/bin/bash
# Replaces email for commits by "Sasha Blashenkov" to git-commits@trall.co

set -e

CORRECT_EMAIL="git-commits@trall.co"
AUTHOR_NAME="Sasha Blashenkov"

# Check if there are commits that need fixing
NEEDS_FIX=$(git log --all --format='%ae' --author="$AUTHOR_NAME" | grep -v "$CORRECT_EMAIL" | head -1)

if [ -z "$NEEDS_FIX" ]; then
    echo "All commits by '$AUTHOR_NAME' already have the correct email."
    exit 0
fi

echo "Found commits with incorrect email. Rewriting history..."

# Stash any uncommitted changes
STASHED=false
if ! git diff --quiet || ! git diff --cached --quiet; then
    echo "Stashing uncommitted changes..."
    git stash push -m "fix-author-email: temporary stash"
    STASHED=true
fi

FILTER_BRANCH_SQUELCH_WARNING=1 git filter-branch -f --env-filter "
if [ \"\$GIT_AUTHOR_NAME\" = \"$AUTHOR_NAME\" ] && [ \"\$GIT_AUTHOR_EMAIL\" != \"$CORRECT_EMAIL\" ]; then
    export GIT_AUTHOR_EMAIL=\"$CORRECT_EMAIL\"
fi
if [ \"\$GIT_COMMITTER_NAME\" = \"$AUTHOR_NAME\" ] && [ \"\$GIT_COMMITTER_EMAIL\" != \"$CORRECT_EMAIL\" ]; then
    export GIT_COMMITTER_EMAIL=\"$CORRECT_EMAIL\"
fi
" --tag-name-filter cat -- --all

# Clean up backup refs
git for-each-ref --format='%(refname)' refs/original/ | xargs -n 1 git update-ref -d 2>/dev/null || true
git reflog expire --expire=now --all
git gc --prune=now

# Restore stashed changes
if [ "$STASHED" = true ]; then
    echo "Restoring stashed changes..."
    git stash pop
fi

echo "Done. Verify with: git log --format='%an <%ae>'"
