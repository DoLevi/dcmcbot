if [ "send" = "$1" ]; then
    tmux send-keys -t "$2" "$3" Enter
elif [ "get" = "$1" ]; then
    tmux capture-pane -t "$2" -p
else
    echo "Something went wrong."
fi