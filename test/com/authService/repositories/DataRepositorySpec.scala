package com.authService.repositories

import com.authService.UnitSpec
import com.authService.models.{Comment, Post}

class DataRepositorySpec extends UnitSpec {
  val repository = new DataRepository

  describe("#getPost") {
    it("should return a post") {
      val subject = repository.getPost(1) match { case Some(post) => post }
      assert(subject == Post(1, "This is a blog post"))
    }
  }

  describe("#getComments") {
    it("should return comments") {
      val subject = repository.getComments(2)
      assert(subject == Seq(Comment(3, 2, "Great, thanks for this post", "Joe Bloggs")))
    }
  }
}
