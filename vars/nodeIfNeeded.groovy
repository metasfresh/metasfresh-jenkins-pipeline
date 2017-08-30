
def call(String nodeLabel, Closure body)
{
  if(env.NODE_LABELS)
  {
    final def labels = env.NODE_LABELS.tokenize(' ')
    if(!labels.contains(nodeLabel))
    {
      error "nodeIfNeeded: Our current node ${env.NODE_NAME} lacks the \'${nodeLabel}\' label. Please make sure to explicitly run on a '${nodeLabel}' node, or on no node at all, to leave it to this step"
    }
    // we are on a 'nodeLabel' node
    echo "nodeIfNeeded: we are called on node ${env.NODE_NAME}; executing given closure without creating another node."

    return body()
  }
  // we are not winthin a node, so start one now
  echo 'nodeIfNeeded: we are not called on a node, so we now open our own node block to execute the given closure'
  node(nodeLabel)
  {
    return body()
  }
}
