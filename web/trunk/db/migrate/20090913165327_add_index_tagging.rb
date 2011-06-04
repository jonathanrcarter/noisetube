class AddIndexTagging < ActiveRecord::Migration
  def self.up
    add_index :taggings, :context
  end

  def self.down
  end
end
