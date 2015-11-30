package test.com.blueskiron.bilby.io.db

import org.scalatest.Sequential

class RegresiveSuite extends Sequential(new TestActiveSlickRepos, new TestDaoFunctions)
