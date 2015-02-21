echo "Sync..." `date`
rsync -avzP --exclude-from './exclude-list' . jupiter:/Users/juri/Documents/github_repos/bilby.io/
