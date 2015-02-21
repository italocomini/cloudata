package com.cloudata.git.model;

import com.cloudata.auth.AuthenticatedUser;
import com.google.protobuf.ByteString;

public abstract class GitUser {

  public abstract ByteString getId();

  public abstract boolean canAccess(GitRepository repo);
}
