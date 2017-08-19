
def call(String nodeLabel, Closure body)
{
  if(env.NODE_LABELS)
  {
    final def labels = env.NODE_LABELS.tokenize(' ')
    if(!labels.contains(nodeLabel))
    {
      error "Our current node ${env.NODE_NAME} lacks the \'${nodeLabel}\' label. Please make sure to explicitly run on a '${nodeLabel}' node, or on no node at all, to leave it to this step"
    }
    // we are on a 'nodeLabel' node
    echo "retrieveReleaseInfo is called on node ${env.NODE_NAME}"
    return body()
  }
  // we are not winthin a node, so start one now
  echo 'retrieveReleaseInfo is not called on a node, so we now open our own node block'
  node(nodeLabel)
  {
    return body()
  }
}
