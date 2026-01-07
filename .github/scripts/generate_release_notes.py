#!/usr/bin/env python3
"""
Generate user-friendly release notes from git commits and code changes.
Keeps it under 500 characters for Google Play.
"""

import subprocess
import sys
import re
from collections import defaultdict

def run_command(cmd):
    """Run a shell command and return output"""
    result = subprocess.run(cmd, shell=True, capture_output=True, text=True)
    return result.stdout.strip()

def get_last_tag():
    """Get the last version tag"""
    tags = run_command("git tag --sort=-v:refname | grep '^v[0-9]' | head -1")
    return tags if tags else None

def get_commits_since_tag(tag):
    """Get commit messages since the last tag"""
    if tag:
        commits = run_command(f"git log {tag}..HEAD --pretty=format:'%s'")
    else:
        commits = run_command("git log --pretty=format:'%s'")
    return commits.split('\n') if commits else []

def categorize_commits(commits):
    """Categorize commits into features, fixes, and improvements"""
    categories = defaultdict(list)

    for commit in commits:
        commit_lower = commit.lower()

        # Skip automated commits
        if 'bump version' in commit_lower or 'generated with claude' in commit_lower:
            continue

        # Categorize based on keywords
        if any(word in commit_lower for word in ['feat', 'feature', 'add', 'implement', 'new']):
            categories['features'].append(commit)
        elif any(word in commit_lower for word in ['fix', 'bug', 'issue', 'resolve', 'correct']):
            categories['fixes'].append(commit)
        elif any(word in commit_lower for word in ['improve', 'enhance', 'update', 'refactor', 'optimize']):
            categories['improvements'].append(commit)
        else:
            categories['other'].append(commit)

    return categories

def make_user_friendly(commit):
    """Convert technical commit message to user-friendly description"""
    # Remove prefixes like "feat:", "fix:", etc.
    commit = re.sub(r'^(feat|feature|fix|bug|improve|update|add|implement|refactor|chore|docs):\s*', '', commit, flags=re.IGNORECASE)

    # Remove technical references
    commit = re.sub(r'\(#\d+\)', '', commit)  # Remove issue numbers
    commit = re.sub(r'\[.*?\]', '', commit)   # Remove [tags]

    # Capitalize first letter
    commit = commit.strip()
    if commit:
        commit = commit[0].upper() + commit[1:]

    # Remove technical jargon
    replacements = {
        'refactor': 'improve',
        'impl': 'implement',
        'deps': 'dependencies',
        'config': 'configuration',
        'repo': 'repository',
    }

    for old, new in replacements.items():
        commit = re.sub(r'\b' + old + r'\b', new, commit, flags=re.IGNORECASE)

    return commit

def generate_release_notes(version_name):
    """Generate release notes"""
    last_tag = get_last_tag()

    if last_tag:
        print(f"Generating release notes from {last_tag} to HEAD", file=sys.stderr)
    else:
        print("No previous tag found, generating initial release notes", file=sys.stderr)

    commits = get_commits_since_tag(last_tag)

    if not commits or (len(commits) == 1 and not commits[0]):
        # No commits since last tag - use generic message
        return f"""SmartCommute v{version_name}

Bug fixes and performance improvements.

Questions or feedback? Leave a review or contact us!

Happy commuting! 🚇"""

    categories = categorize_commits(commits)

    # Build release notes
    notes = [f"SmartCommute v{version_name}\n"]

    # Add features
    if categories['features']:
        notes.append("New:")
        for commit in categories['features'][:2]:  # Max 2 features
            friendly = make_user_friendly(commit)
            if len(friendly) > 60:
                friendly = friendly[:57] + "..."
            notes.append(f"• {friendly}")

    # Add fixes
    if categories['fixes']:
        notes.append("\nFixed:")
        for commit in categories['fixes'][:2]:  # Max 2 fixes
            friendly = make_user_friendly(commit)
            if len(friendly) > 60:
                friendly = friendly[:57] + "..."
            notes.append(f"• {friendly}")

    # Add improvements
    if categories['improvements']:
        if not categories['features'] and not categories['fixes']:
            notes.append("Improvements:")
        else:
            notes.append("\nImproved:")
        for commit in categories['improvements'][:2]:  # Max 2 improvements
            friendly = make_user_friendly(commit)
            if len(friendly) > 60:
                friendly = friendly[:57] + "..."
            notes.append(f"• {friendly}")

    # If we have nothing to show, use generic message
    if len(notes) == 1:
        return f"""SmartCommute v{version_name}

Bug fixes and performance improvements.

Questions or feedback? Leave a review or contact us!

Happy commuting! 🚇"""

    # Add footer
    notes.append("\nQuestions or feedback? Leave a review or contact us!")
    notes.append("\nHappy commuting! 🚇")

    release_notes = '\n'.join(notes)

    # Ensure it's under 500 characters
    if len(release_notes) > 500:
        # Truncate and add ellipsis
        release_notes = release_notes[:480] + "...\n\nHappy commuting! 🚇"

    return release_notes

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: generate_release_notes.py <version_name>", file=sys.stderr)
        sys.exit(1)

    version_name = sys.argv[1]
    release_notes = generate_release_notes(version_name)

    print(release_notes)
    print(f"\n[Length: {len(release_notes)} characters]", file=sys.stderr)
